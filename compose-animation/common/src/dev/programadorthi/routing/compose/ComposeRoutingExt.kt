package dev.programadorthi.routing.compose

import dev.programadorthi.routing.core.Routing

public fun Routing.canPop(): Boolean = contentList.size > 1

public fun Routing.pop(result: Any? = null) {
    if (!canPop()) return

    val last = contentList.removeLastOrNull() ?: return
    last.popped = true
    last.popResult = result
    this.poppedEntry = last
}

public fun Routing.poppedEntry(): ComposeEntry? = poppedEntry
