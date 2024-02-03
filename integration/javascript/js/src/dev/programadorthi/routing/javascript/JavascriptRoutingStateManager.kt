package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.parametersOf
import kotlinx.browser.window
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object JavascriptRoutingStateManager {
    fun init(routing: Routing) {
        // First time or page refresh we try continue from last state
        routing.tryNotifyTheRoute(state = window.history.state)

        resetOnPopStateEvent(routing)
    }

    suspend fun replaceAll(
        call: ApplicationCall,
        routing: Routing,
    ) {
        while (true) {
            window.history.replaceState(
                title = "",
                url = null,
                data = null,
            )
            window.history.go(-1)
            val forceBreak =
                runCatching {
                    withTimeout(1_000) {
                        suspendCoroutine { continuation ->
                            window.onpopstate = { event ->
                                val state = event.state.deserialize()
                                continuation.resume(state == null)
                            }
                        }
                    }
                }.getOrDefault(true)
            if (forceBreak) {
                break
            }
        }

        window.history.replaceState(
            title = "routing",
            url = call.uri,
            data = call.serialize(),
        )

        resetOnPopStateEvent(routing)
    }

    private fun resetOnPopStateEvent(routing: Routing) {
        window.onpopstate = { event ->
            routing.tryNotifyTheRoute(state = event.state)
        }
    }

    private fun Routing.tryNotifyTheRoute(state: Any?) {
        val javascriptState = state.deserialize() ?: return
        val call =
            ApplicationCall(
                application = application,
                name = javascriptState.name,
                uri = javascriptState.uri,
                routeMethod = RouteMethod.parse(javascriptState.routeMethod),
                parameters = parametersOf(javascriptState.parameters),
            )
        call.neglect = true
        execute(call)
    }
}
