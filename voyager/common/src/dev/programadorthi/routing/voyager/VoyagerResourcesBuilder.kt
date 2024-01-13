package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.core.OptionalParameterRouteSelector
import dev.programadorthi.routing.core.ParameterRouteSelector
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationCallPipeline
import dev.programadorthi.routing.core.application.application
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.plugin
import dev.programadorthi.routing.core.errors.BadRequestException
import dev.programadorthi.routing.core.method
import dev.programadorthi.routing.core.route
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

/**
 * Registers a typed handler [body] for a resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.screen(noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Screen): Route {
    val serializer = serializer<T>()
    return screen(serializer) {
        handle(serializer, body)
    }
}

/**
 * Registers a typed handler for a [Screen] defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 */
public inline fun <reified T : Screen> Route.screen(): Route = screen<T> { screen -> screen }

/**
 * Registers a typed handler [body] for a [VoyagerRouteMethod] resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.screen(
    method: RouteMethod,
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Screen,
): Route {
    val serializer = serializer<T>()
    lateinit var builtRoute: Route
    screen(serializer) {
        builtRoute =
            method(method) {
                handle(serializer, body)
            }
    }
    return builtRoute
}

/**
 * Registers a typed handler for a [VoyagerRouteMethod] [Screen] defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 */
public inline fun <reified T : Screen> Route.screen(method: RouteMethod): Route = screen<T>(method) { screen -> screen }

@PublishedApi
internal val ResourceInstanceKey: AttributeKey<Any> = AttributeKey("ResourceInstance")

/**
 * Registers a route [body] for a resource defined by the [T] class.
 *
 * @param serializer is used to decode the parameters of the request to an instance of the typed resource [T].
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 */
public fun <T : Any> Route.screen(
    serializer: KSerializer<T>,
    body: Route.() -> Unit,
): Route {
    val resources = application.plugin(VoyagerResources)
    val path = resources.resourcesFormat.encodeToPathPattern(serializer)
    val queryParameters = resources.resourcesFormat.encodeToQueryParameters(serializer)
    var route = this
    // Required for register to parents
    route(path = path, name = null) {
        route =
            queryParameters.fold(this) { entry, query ->
                val selector =
                    if (query.isOptional) {
                        OptionalParameterRouteSelector(query.name)
                    } else {
                        ParameterRouteSelector(query.name)
                    }
                entry.createChild(selector)
            }.apply(body)
    }
    return route
}

/**
 * Registers a handler [body] for a resource defined by the [T] class.
 *
 * @param serializer is used to decode the parameters of the request to an instance of the typed resource [T].
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
@PublishedApi
internal fun <T : Any> Route.handle(
    serializer: KSerializer<T>,
    body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Screen,
) {
    intercept(ApplicationCallPipeline.Plugins) {
        val resources = application.plugin(VoyagerResources)
        try {
            val resource =
                resources.resourcesFormat.decodeFromParameters(serializer, call.parameters)
            call.attributes.put(ResourceInstanceKey, resource)
        } catch (cause: Throwable) {
            throw BadRequestException("Can't transform call to resource", cause)
        }
    }

    handle {
        @Suppress("UNCHECKED_CAST")
        val resource = call.attributes[ResourceInstanceKey] as T
        screen {
            when (resource) {
                is Screen -> resource
                else -> body(resource)
            }
        }
    }
}

public inline fun <reified T : Any> Routing.unregisterScreen() {
    val serializer = serializer<T>()
    val route = screen(serializer) {}
    unregisterRoute(route)
}
