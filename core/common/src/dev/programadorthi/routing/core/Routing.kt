/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationEnvironment
import dev.programadorthi.routing.core.application.ApplicationStarted
import dev.programadorthi.routing.core.application.ApplicationStarting
import dev.programadorthi.routing.core.application.ApplicationStopped
import dev.programadorthi.routing.core.application.ApplicationStopping
import dev.programadorthi.routing.core.application.BaseApplicationPlugin
import dev.programadorthi.routing.core.application.Plugin
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.install
import dev.programadorthi.routing.core.application.log
import dev.programadorthi.routing.core.errors.BadRequestException
import dev.programadorthi.routing.core.errors.MissingRequestParameterException
import dev.programadorthi.routing.core.errors.RouteNotFoundException
import io.ktor.events.EventDefinition
import io.ktor.events.Events
import io.ktor.http.Parameters
import io.ktor.http.Url
import io.ktor.http.plus
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import io.ktor.util.logging.isTraceEnabled
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.execute
import io.ktor.utils.io.KtorDsl
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A root routing node.
 * You can learn more about routing in Ktor from [Routing](https://ktor.io/docs/routing-in-ktor.html).
 */
@KtorDsl
public class Routing internal constructor(
    internal val application: Application,
) : Route(
        parent = application.environment.parentRouting,
        selector = RootRouteSelector(application.environment.rootPath),
        application.environment.developmentMode,
        application.environment,
    ) {
    private val tracers = mutableListOf<(RoutingResolveTrace) -> Unit>()
    private val namedRoutes = mutableMapOf<String, Route>()
    private var disposed = false

    init {
        addDefaultTracing()
    }

    public fun execute(call: ApplicationCall) {
        var current: Routing? = this
        while (current?.disposed == true && current.parent != null) {
            current = current.parent?.asRouting
        }
        val scope = current?.application ?: return
        with(scope) {
            launch {
                execute(call)
            }
        }
    }

    public fun unregisterNamed(name: String) {
        val route = namedRoutes.remove(name) ?: return
        unregisterRoute(route)
    }

    public fun unregisterPath(path: String) {
        val route = createRouteFromPath(path)
        unregisterRoute(route)
    }

    public fun unregisterRoute(route: Route) {
        removeChild(route)
        unregisterFromParents(route)
    }

    /**
     * Registers a function used to trace route resolution.
     * Might be useful if you need to understand why a route isn't executed.
     * To learn more, see [Tracing routes](https://ktor.io/docs/tracing-routes.html).
     */
    public fun trace(block: (RoutingResolveTrace) -> Unit) {
        tracers.add(block)
    }

    public fun dispose() {
        environment.safeRiseEvent(ApplicationStopping, application)
        unregisterFromParents(this)
        childList.clear()
        namedRoutes.clear()
        application.dispose()
        disposed = true
        environment.safeRiseEvent(ApplicationStopped, application)
    }

    internal fun registerNamed(
        name: String,
        route: Route,
    ) {
        check(!namedRoutes.containsKey(name)) {
            "Duplicated named route. Found '$name' to ${namedRoutes[name]} and $route"
        }
        namedRoutes[name] = route
    }

    private fun removeChild(route: Route) {
        val parentRoute = route.parent ?: return
        parentRoute.childList.remove(route)
        if (parentRoute.childList.isEmpty()) {
            removeChild(parentRoute)
        }
    }

    private fun unregisterFromParents(route: Route) {
        val parentRouting = route.asRouting?.parent
        if (parentRouting is Routing) {
            val validPath = route.toString().substringAfter(parentRouting.toString())
            val toRemove = parentRouting.createRouteFromPath(validPath)
            removeChild(toRemove)
            unregisterFromParents(toRemove)
        }
    }

    private fun mapNameToPath(
        name: String,
        pathParameters: Parameters,
    ): String {
        val namedRoute =
            namedRoutes[name]
                ?: throw RouteNotFoundException(message = "Named route not found with name: $name")
        val routeSelectors = namedRoute.allSelectors()
        val skipPathParameters =
            routeSelectors.run {
                isEmpty() || all { selector -> selector is PathSegmentConstantRouteSelector }
            }
        if (skipPathParameters) {
            return namedRoute.toString()
        }

        return tryReplacePathParameters(
            routeName = name,
            namedRoute = namedRoute,
            pathParameters = pathParameters,
            routeSelectors = routeSelectors,
        )
    }

    private fun tryReplacePathParameters(
        routeName: String,
        namedRoute: Route,
        pathParameters: Parameters,
        routeSelectors: List<RouteSelector>,
    ): String {
        val destination = mutableListOf<String>()
        for (selector in routeSelectors) {
            when (selector) {
                // Check constant value in path as 'hello' in /hello
                is PathSegmentConstantRouteSelector -> {
                    destination += selector.value
                }

                // Check optional {value?} path parameter
                is PathSegmentOptionalParameterRouteSelector -> {
                    val param = pathParameters[selector.name]
                    if (!param.isNullOrBlank()) {
                        destination += param
                    }
                }

                // Check required {value} path parameter
                is PathSegmentParameterRouteSelector -> {
                    val param = pathParameters[selector.name]
                    assertParameters(
                        routeName = routeName,
                        namedRoute = namedRoute,
                        parameterName = selector.name,
                        predicate = param::isNullOrBlank,
                    )
                    destination += param!!
                }

                // Check tailcard {value...} or {...} path parameter
                is PathSegmentTailcardRouteSelector -> {
                    if (selector.name.isNotBlank()) {
                        val values = pathParameters.getAll(selector.name)
                        assertParameters(
                            routeName = routeName,
                            namedRoute = namedRoute,
                            parameterName = selector.name,
                            predicate = values::isNullOrEmpty,
                        )
                        destination.addAll(values!!)
                    } else {
                        assertParameters(
                            routeName = routeName,
                            namedRoute = namedRoute,
                            parameterName = selector.name,
                            predicate = pathParameters::isEmpty,
                        )
                        pathParameters.forEach { _, values ->
                            destination.addAll(values)
                        }
                    }
                }

                // Check * path parameter
                is PathSegmentWildcardRouteSelector -> {
                    val names = pathParameters.names()
                    assertParameters(
                        routeName = routeName,
                        namedRoute = namedRoute,
                        parameterName = selector.toString(),
                        predicate = names::isEmpty,
                    )
                    val values = pathParameters.getAll(names.first())
                    assertParameters(
                        routeName = routeName,
                        namedRoute = namedRoute,
                        parameterName = selector.toString(),
                        predicate = values::isNullOrEmpty,
                    )
                    destination.addAll(values!!)
                }
            }
        }
        return destination.joinToString(separator = "/", prefix = "/")
    }

    private fun addDefaultTracing() {
        if (!application.log.isTraceEnabled) return

        tracers.add {
            application.log.trace(it.buildText())
        }
    }

    @Throws(MissingRequestParameterException::class)
    private fun assertParameters(
        routeName: String,
        namedRoute: Route,
        parameterName: String,
        predicate: () -> Boolean,
    ) {
        if (predicate()) {
            throw MissingRequestParameterException(
                parameterName = parameterName,
                message = "Parameter $parameterName is missing to route named '$routeName' and path: $namedRoute",
            )
        }
    }

    private suspend fun interceptor(context: PipelineContext<Unit, ApplicationCall>) {
        var call = context.call

        // Getting query parameters provided in the URI
        var queryParameters = Parameters.Empty
        if (call.uri.isNotBlank()) {
            queryParameters = Url(call.uri).parameters
        }

        // Transforming a name to a URI
        val uri =
            if (call.name.isBlank()) {
                call.uri
            } else {
                mapNameToPath(
                    name = call.name,
                    pathParameters = call.parameters,
                )
            }

        if (uri.isBlank()) {
            throw BadRequestException("Received call with an empty uri: $call")
        }

        call =
            ResolveApplicationCall(
                coroutineContext = context.coroutineContext,
                previousCall = call,
                params = call.parameters + queryParameters,
                uri = uri,
            )

        val resolveContext = RoutingResolveContext(this, call, tracers)
        when (val resolveResult = resolveContext.resolve()) {
            is RoutingResolveResult.Failure -> {
                val routing =
                    parent?.asRouting
                        ?: throw RouteNotFoundException(message = resolveResult.reason)
                routing.execute(context.call)
            }

            is RoutingResolveResult.Success -> {
                val routingCallPipeline = resolveResult.route.buildPipeline()
                val routingCall =
                    RoutingApplicationCall(
                        coroutineContext = context.coroutineContext,
                        routeMethod = call.routeMethod,
                        previousCall = call,
                        route = resolveResult.route,
                        parameters = resolveResult.parameters,
                    )
                application.environment.monitor.raise(RoutingCallStarted, routingCall)
                try {
                    routingCallPipeline.execute(routingCall)
                } finally {
                    application.environment.monitor.raise(RoutingCallFinished, routingCall)
                }
            }
        }
    }

    /**
     * An installation object of the [Routing] plugin.
     */
    @Suppress("PublicApiImplicitType")
    public companion object Plugin : BaseApplicationPlugin<Application, Route, Routing> {
        /**
         * A definition for an event that is fired when routing-based call processing starts.
         */
        internal val RoutingCallStarted: EventDefinition<RoutingApplicationCall> = EventDefinition()

        /**
         * A definition for an event that is fired when routing-based call processing is finished.
         */
        internal val RoutingCallFinished: EventDefinition<RoutingApplicationCall> =
            EventDefinition()

        override val key: AttributeKey<Routing> = AttributeKey("Routing")

        override fun install(
            pipeline: Application,
            configure: Route.() -> Unit,
        ): Routing {
            val routing = Routing(pipeline).apply(configure)
            pipeline.intercept(Call) {
                routing.interceptor(this)
            }
            return routing
        }
    }
}

/**
 * Gets an [Application] for this [Route] by scanning the hierarchy to the root.
 */
public val Route.application: Application
    get() =
        when (this) {
            is Routing -> application
            else ->
                parent?.application ?: throw UnsupportedOperationException(
                    "Cannot retrieve application from unattached routing entry",
                )
        }

public val Route.asRouting: Routing?
    get() =
        when (this) {
            is Routing -> this
            else -> parent?.asRouting
        }

public fun <B : Any, F : Any> Route.install(
    plugin: Plugin<Application, B, F>,
    configure: B.() -> Unit = {},
): F = application.install(plugin, configure)

public fun Routing.call(
    name: String = "",
    uri: String = "",
    routeMethod: RouteMethod = RouteMethod.Empty,
    attributes: Attributes = Attributes(),
    parameters: Parameters = Parameters.Empty,
) {
    execute(
        ApplicationCall(
            application = application,
            name = name,
            uri = uri,
            routeMethod = routeMethod,
            attributes = attributes,
            parameters = parameters,
        ),
    )
}

@KtorDsl
public fun routing(
    rootPath: String = "/",
    parent: Routing? = null,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    log: Logger = KtorSimpleLogger("kotlin-routing"),
    developmentMode: Boolean = false,
    configuration: Route.() -> Unit,
): Routing {
    check(parent == null || rootPath != "/") {
        "Child routing cannot have root path with '/' only. Please, provide a path to your child routing"
    }
    val environment =
        ApplicationEnvironment(
            parentCoroutineContext = parentCoroutineContext,
            developmentMode = developmentMode,
            rootPath = rootPath,
            parentRouting = parent,
            log = log,
            monitor = Events(),
        )
    val application = Application(environment)
    environment.safeRiseEvent(ApplicationStarting, application)
    val instance = application.install(Routing, configuration)
    environment.safeRiseEvent(ApplicationStarted, application)
    return instance
}

private fun ApplicationEnvironment?.safeRiseEvent(
    event: EventDefinition<Application>,
    application: Application,
) {
    val instance = this ?: return
    runCatching {
        instance.monitor.raise(event, application)
    }.onFailure { cause ->
        instance.log.error("One or more of the handlers thrown an exception", cause)
    }
}
