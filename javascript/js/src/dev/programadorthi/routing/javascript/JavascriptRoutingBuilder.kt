package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.route
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext
import kotlinx.browser.window
import org.w3c.dom.Element
import toData

@KtorDsl
public fun Route.jsRoute(
    path: String,
    name: String? = null,
    body: PipelineContext<Unit, ApplicationCall>.() -> Element,
): Route = route(path = path, name = name) { jsRoute(body) }

@KtorDsl
public fun Route.jsRoute(
    path: String,
    method: RouteMethod,
    name: String? = null,
    body: PipelineContext<Unit, ApplicationCall>.() -> Element,
): Route = route(path = path, name = name, method = method) { jsRoute(body) }

@KtorDsl
public fun Route.jsRoute(
    body: PipelineContext<Unit, ApplicationCall>.() -> Element,
) {
    handle {
        call.destination = body(this)
        window.history.pushState(data = call.toData(), title = "", url = call.uri)
    }
}
