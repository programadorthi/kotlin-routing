package dev.programadorthi.routing.events.resources

import dev.programadorthi.routing.core.OptionalParameterRouteSelector
import dev.programadorthi.routing.core.ParameterRouteSelector
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationCallPipeline
import dev.programadorthi.routing.core.application.application
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.plugin
import dev.programadorthi.routing.core.createRouteFromPath
import dev.programadorthi.routing.core.errors.BadRequestException
import dev.programadorthi.routing.core.method
import dev.programadorthi.routing.events.EventRouteMethod
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.serializer

@PublishedApi
internal val EventResourceInstanceKey: AttributeKey<Any> = AttributeKey("EventResourceInstance")

/**
 * Registers a typed handler [body] by the [T] class.
 *
 * A class [T] **must** be annotated with [Event].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.event(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    val serializer = serializer<T>()
    val resources = application.plugin(EventResources)
    val path = resources.resourcesFormat.encodeToPathPattern(serializer)
    val queryParameters = resources.resourcesFormat.encodeToQueryParameters(serializer)
    val route = createRouteFromPath(path = path, name = null)

    val routeWithQueryParameters = queryParameters.fold(route) { entry, query ->
        val selector = if (query.isOptional) {
            OptionalParameterRouteSelector(query.name)
        } else {
            ParameterRouteSelector(query.name)
        }
        entry.createChild(selector)
    }

    return routeWithQueryParameters.method(EventRouteMethod) {
        intercept(ApplicationCallPipeline.Plugins) {
            val resourcesPlugin = application.plugin(EventResources)
            runCatching {
                val resource = resourcesPlugin.resourcesFormat.decodeFromParameters(
                    serializer,
                    call.parameters
                )
                call.attributes.put(EventResourceInstanceKey, resource)
            }.getOrElse { cause ->
                throw BadRequestException("Can't transform call to resource", cause)
            }
        }

        handle {
            val resource = call.attributes[EventResourceInstanceKey] as T
            body(resource)
        }
    }
}

public inline fun <reified T : Any> Routing.unregisterEvent() {
    val route = event<T> { }
    unregisterRoute(route)
}
