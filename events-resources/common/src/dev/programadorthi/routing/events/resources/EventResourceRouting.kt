package dev.programadorthi.routing.events.resources

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.plugin
import dev.programadorthi.routing.events.emitEvent
import dev.programadorthi.routing.events.redirectToEvent
import kotlinx.serialization.serializer

public inline fun <reified T : Any> Routing.emitEvent(resource: T) {
    val format = plugin(EventResources).resourcesFormat
    val serializer = serializer<T>()
    emitEvent(
        name = format.encodeToPathPattern(serializer),
        parameters = format.encodeToParameters(serializer, resource),
    )
}

public inline fun <reified T : Any> ApplicationCall.redirectTo(resource: T) {
    val format = application.plugin(EventResources).resourcesFormat
    val serializer = serializer<T>()
    redirectToEvent(
        name = format.encodeToPathPattern(serializer),
        parameters = format.encodeToParameters(serializer, resource),
    )
}
