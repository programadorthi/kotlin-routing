/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationEnvironment
import dev.programadorthi.routing.core.application.BaseApplicationPlugin
import dev.programadorthi.routing.core.application.Plugin
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.install
import dev.programadorthi.routing.core.application.log
import dev.programadorthi.routing.core.errors.MissingRequestParameterException
import dev.programadorthi.routing.core.errors.RouteNotFoundException
import dev.programadorthi.routing.core.errors.TooManyRedirectException
import dev.programadorthi.routing.core.http.Parameters
import io.ktor.util.AttributeKey
import io.ktor.util.KtorDsl
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import io.ktor.util.logging.isTraceEnabled
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.execute
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
    parent = null,
    selector = RootRouteSelector(""),
    application.environment.developmentMode,
    application.environment
) {
    private val tracers = mutableListOf<(RoutingResolveTrace) -> Unit>()
    private val namedRoutes = mutableMapOf<String, Route>()

    init {
        addDefaultTracing()
    }

    public fun push(path: String, parameters: Parameters = Parameters.Empty) {
        executeCall(
            NavigationApplicationCall(
                application = application,
                uri = path,
                parameters = parameters,
            )
        )
    }

    public fun pushNamed(
        name: String,
        parameters: Parameters = Parameters.Empty,
        pathReplacements: Parameters = Parameters.Empty,
    ) {
        executeCall(
            NavigationApplicationCall(
                application = application,
                name = name,
                parameters = parameters,
                pathReplacements = pathReplacements,
            )
        )
    }

    public fun replace(path: String, parameters: Parameters = Parameters.Empty) {
        executeCall(
            NavigationApplicationCall(
                application = application,
                uri = path,
                parameters = parameters,
                replaceCurrent = true,
            )
        )
    }

    public fun replaceAll(path: String, parameters: Parameters = Parameters.Empty) {
        executeCall(
            NavigationApplicationCall(
                application = application,
                uri = path,
                parameters = parameters,
                replaceAll = true,
            )
        )
    }

    public fun replaceNamed(
        name: String,
        parameters: Parameters = Parameters.Empty,
        pathReplacements: Parameters = Parameters.Empty,
    ) {
        executeCall(
            NavigationApplicationCall(
                application = application,
                name = name,
                parameters = parameters,
                pathReplacements = pathReplacements,
                replaceCurrent = true,
            )
        )
    }

    public fun replaceAllNamed(
        name: String,
        parameters: Parameters = Parameters.Empty,
        pathReplacements: Parameters = Parameters.Empty,
    ) {
        executeCall(
            NavigationApplicationCall(
                application = application,
                name = name,
                parameters = parameters,
                pathReplacements = pathReplacements,
                replaceAll = true,
            )
        )
    }

    /**
     * Registers a function used to trace route resolution.
     * Might be useful if you need to understand why a route isn't executed.
     * To learn more, see [Tracing routes](https://ktor.io/docs/tracing-routes.html).
     */
    public fun trace(block: (RoutingResolveTrace) -> Unit) {
        tracers.add(block)
    }

    internal fun dispose() {
        application.dispose()
    }

    internal fun registerNamed(name: String, route: Route) {
        check(!namedRoutes.containsKey(name)) {
            "Duplicated named route. Found '$name' to ${namedRoutes[name]} and $route"
        }
        namedRoutes[name] = route
    }

    internal fun mapNameToPath(name: String, pathReplacements: Parameters): String {
        val namedRoute =
            namedRoutes[name] ?: throw RouteNotFoundException(message = "Named route not found with name: $name")
        val routeSelectors = namedRoute.allSelectors()
        val skipPathParameters = routeSelectors.run {
            isEmpty() || all { selector -> selector is PathSegmentConstantRouteSelector }
        }
        if (skipPathParameters) {
            return namedRoute.toString()
        }

        return tryReplacePathParameters(
            routeName = name,
            namedRoute = namedRoute,
            pathParameters = pathReplacements,
            routeSelectors = routeSelectors,
        )
    }

    private fun executeCall(call: ApplicationCall) {
        with(application) {
            launch {
                execute(call)
            }
        }
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
        return destination.joinToString(separator = "/")
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
                message = "Parameter $parameterName is missing to route named '$routeName' and path: $namedRoute"
            )
        }
    }

    private suspend fun interceptor(context: PipelineContext<Unit, ApplicationCall>) {
        val call = mapCallBeforeResolve(context.call)
        val resolveContext = RoutingResolveContext(this, call, tracers)
        when (val resolveResult = resolveContext.resolve()) {
            is RoutingResolveResult.Failure -> throw RouteNotFoundException(message = resolveResult.reason)

            is RoutingResolveResult.Success -> {
                val routingCallPipeline = resolveResult.route.buildPipeline()
                val routingCall = RoutingApplicationCall(
                    previousCall = call,
                    route = resolveResult.route,
                    coroutineContext = context.coroutineContext,
                    parameters = resolveResult.parameters,
                )
                routingCallPipeline.execute(routingCall)
            }
        }
    }

    private fun mapCallBeforeResolve(other: ApplicationCall): ApplicationCall {
        var call = other
        if (call is RedirectApplicationCall) {
            if (call.attempt > application.environment.maxRedirectAttempts) {
                throw TooManyRedirectException("Too many redirection. Last path try: ${call.uri}")
            }
            if (call.name.isNotBlank()) {
                call = RedirectApplicationCall(
                    previousCall = call.previousCall,
                    name = "",
                    attempt = call.attempt,
                    coroutineContext = call.coroutineContext,
                    uri = mapNameToPath(name = call.name, pathReplacements = call.parameters),
                )
            }
        } else if (call is NavigationApplicationCall) {
            if (call.name.isNotBlank()) {
                call = call.copy(
                    name = "",
                    uri = mapNameToPath(name = call.name, pathReplacements = call.pathReplacements)
                )
            }
        }
        return call
    }

    /**
     * An installation object of the [Routing] plugin.
     */
    @Suppress("PublicApiImplicitType")
    public companion object Plugin : BaseApplicationPlugin<Application, Routing, Routing> {

        override val key: AttributeKey<Routing> = AttributeKey("Routing")

        override fun install(pipeline: Application, configure: Routing.() -> Unit): Routing {
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
    get() = when (this) {
        is Routing -> application
        else -> parent?.application ?: throw UnsupportedOperationException(
            "Cannot retrieve application from unattached routing entry"
        )
    }

public fun <B : Any, F : Any> Route.install(
    plugin: Plugin<Application, B, F>,
    configure: B.() -> Unit = {}
): F = application.install(plugin, configure)

@KtorDsl
public fun routing(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    log: Logger = KtorSimpleLogger("VoyagerRouting"),
    startDestination: String = "/",
    developmentMode: Boolean = false,
    maxRedirectAttempts: Int = 5,
    configuration: Routing.() -> Unit
): Routing {
    val environment = ApplicationEnvironment(
        parentCoroutineContext = parentCoroutineContext,
        log = log,
        starterPath = startDestination,
        developmentMode = developmentMode,
        maxRedirectAttempts = maxRedirectAttempts,
    )
    return with(Application(environment)) {
        install(Routing, configuration)
    }
}
