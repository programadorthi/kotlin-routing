package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application

public inline fun <reified T : Any> Routing.execute(resource: T) {
    val destination = application.href(resource)
    execute(
        ResourceApplicationCall(
            application = application,
            uri = destination
        )
    )
}
