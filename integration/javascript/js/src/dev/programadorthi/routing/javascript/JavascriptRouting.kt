package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
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

    JavascriptRoutingStateManager.init(routing)
}
