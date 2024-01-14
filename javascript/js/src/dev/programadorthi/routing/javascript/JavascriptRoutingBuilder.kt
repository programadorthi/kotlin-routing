package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.application
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.route
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.KtorDsl
import kotlinx.browser.window
import org.w3c.dom.Element

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
public fun Route.jsRoute(body: PipelineContext<Unit, ApplicationCall>.() -> Element) {
    handle {
        application.routingFlow.emit(body(this))

        when (call.routeMethod) {
            RouteMethod.Push -> window.history.pushState(
                data = call.toData(),
                title = "",
                url = call.uri
            )

            RouteMethod.Replace -> window.history.replaceState(
                data = call.toData(),
                title = "",
                url = call.uri
            )

            else -> TODO("Not implemented yet")
        }
    }
}
