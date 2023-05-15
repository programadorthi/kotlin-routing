package dev.programadorthi.routing.events

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.route
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineInterceptor

@KtorDsl
public fun Route.event(
    name: String,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    check(parent == null) {
        "An event must be in a top-level route declaration. It cannot be a sub-route"
    }
    return route(
        path = name.checkForSlash(),
        name = null,
        method = EventRouteMethod,
    ) {
        handle(body)
    }
}
