package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.Routing
import kotlinx.browser.window

@Suppress("UnusedReceiverParameter")
public fun Routing.pop() {
    window.history.go(-1)
}
