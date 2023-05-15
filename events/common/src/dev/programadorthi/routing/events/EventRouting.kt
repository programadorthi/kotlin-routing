package dev.programadorthi.routing.events

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.redirectToPath
import io.ktor.http.Parameters

public fun Routing.emitEvent(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    execute(
        EventApplicationCall(
            application = application,
            uri = name.checkForSlash().normalizedEvent(),
            parameters = parameters,
        )
    )
}

public fun ApplicationCall.redirectToEvent(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    redirectToPath(
        path = name.checkForSlash().normalizedEvent(),
        parameters = parameters,
    )
}
