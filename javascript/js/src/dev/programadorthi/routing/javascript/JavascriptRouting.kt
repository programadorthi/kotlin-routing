package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.parametersOf
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import org.w3c.dom.Element

public fun render(
    routing: Routing,
    root: Element,
    initial: Element,
) {
    with(routing.application) {
        routingFlow = MutableStateFlow(initial)

        launch {
            routingFlow.collect { child ->
                root.clear()
                root.appendChild(child)
            }
        }
    }

    // First time or page refresh we try continue from last state
    routing.tryNotifyTheRoute(state = window.history.state)

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
