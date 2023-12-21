package dev.programadorthi.routing.events

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.redirectToPath
import dev.programadorthi.routing.core.call
import io.ktor.http.Parameters

public fun Routing.emitEvent(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        uri = name,
        parameters = parameters,
        routeMethod = EventRouteMethod,
    )
}

public fun ApplicationCall.redirectToEvent(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    redirectToPath(
        path = name,
        parameters = parameters,
    )
}
