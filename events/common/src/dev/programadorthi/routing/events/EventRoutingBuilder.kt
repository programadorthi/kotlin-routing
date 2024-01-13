package dev.programadorthi.routing.events

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.route
import io.ktor.util.pipeline.PipelineInterceptor
import io.ktor.utils.io.KtorDsl

@KtorDsl
public fun Route.event(
    name: String,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    check(parent == null || this is Routing) {
        "An event cannot be a child of other Route"
    }
    return route(
        path = name,
        name = null,
        method = EventRouteMethod,
    ) {
        handle(body)
    }
}

public fun Routing.unregisterEvent(name: String) {
    val route =
        route(
            path = name,
            name = null,
            method = EventRouteMethod,
        ) { }
    unregisterRoute(route)
}
