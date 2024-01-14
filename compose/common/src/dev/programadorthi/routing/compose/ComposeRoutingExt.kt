package dev.programadorthi.routing.compose

import dev.programadorthi.routing.core.Routing

public fun Routing.canPop(): Boolean = contentList.size > 1

public fun Routing.pop() {
    contentList.removeLastOrNull()
    // TODO: add support to send result
}
