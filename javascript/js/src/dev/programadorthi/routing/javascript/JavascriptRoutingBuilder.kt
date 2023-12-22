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
import kotlin.js.json

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
        val data = json(
            METHOD_KEY to call.routeMethod.value,
            URI_KEY to call.uri,
        )
        window.history.pushState(data = data, title = "", url = call.uri)
    }
}
