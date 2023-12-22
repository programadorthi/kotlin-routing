package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import org.w3c.dom.Element
import org.w3c.dom.PopStateEvent
import toCall

public fun render(
    routing: Routing,
    root: Element,
) {
    with(routing.application) {
        routingFlow = MutableStateFlow(null)

        launch {
            routingFlow.collect { child ->
                if (child != null) {
                    root.clear()
                    root.appendChild(child)
                }
            }
        }
    }

    window.onpopstate = { event ->
        onPopState(routing = routing, event = event)
    }
}

private fun onPopState(routing: Routing, event: PopStateEvent) {
    val data = event.state as? String
    if (data.isNullOrBlank()) return

    val call = data.toCall(routing.application)
    routing.execute(call)
}
