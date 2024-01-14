package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.Routing
import kotlinx.browser.window

public fun Routing.pop() {
    window.history.replaceState(data = null, title = "", url = null)
    window.history.back()
}
