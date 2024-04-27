package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.application
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.asRouting
import dev.programadorthi.routing.core.route
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.KtorDsl
import kotlinx.browser.window
import org.w3c.dom.Element

private const val HASH_PREFIX = "/#"

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
    val routing = asRouting ?: error("A route must be a Routing child")

    handle {
        application.routingFlow.emit(body(this))

        if (call.neglect) {
            return@handle
        }

        if (application.historyMode == JavascriptRoutingHistoryMode.Memory) {
            when (call.routeMethod) {
                RouteMethod.Push -> application.callStack += call
                RouteMethod.Replace -> {
                    application.callStack.removeLastOrNull()
                    application.callStack += call
                }

                RouteMethod.ReplaceAll -> {
                    application.callStack.clear()
                    application.callStack += call
                }
            }
            return@handle
        }

        when (call.routeMethod) {
            RouteMethod.Push ->
                window.history.pushState(
                    title = "routing",
                    url = call.uriToAddressBar(),
                    data = call.serialize(),
                )

            RouteMethod.Replace ->
                window.history.replaceState(
                    title = "routing",
                    url = call.uriToAddressBar(),
                    data = call.serialize(),
                )

            RouteMethod.ReplaceAll -> JavascriptRoutingStateManager.replaceAll(call, routing)
        }
    }
}

internal fun ApplicationCall.uriToAddressBar(): String {
    return when {
        application.historyMode != JavascriptRoutingHistoryMode.Hash -> uri

        else -> HASH_PREFIX + uri
    }
}
