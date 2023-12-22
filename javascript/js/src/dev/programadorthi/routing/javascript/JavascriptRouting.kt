package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.call
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import org.w3c.dom.Element
import org.w3c.dom.PopStateEvent
import kotlin.js.Json

internal const val METHOD_KEY = "method"
internal const val URI_KEY = "uri"

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

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
private fun onPopState(routing: Routing, event: PopStateEvent) {
    val json = event.state as? Json ?: return
    val uri = json[URI_KEY] as? String
    val method = json[METHOD_KEY] as? String
    if (uri.isNullOrBlank() || method.isNullOrBlank()) return
    // TODO: add route method and parameters support
    routing.call(uri = uri)
}
