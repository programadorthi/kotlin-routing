package dev.programadorthi.routing.compose

import dev.programadorthi.routing.core.Routing

internal actual fun Routing.popOnPlatform(
    result: Any?,
    fallback: () -> Unit,
) {
    fallback()
}

public actual val Routing.canPop: Boolean
    get() = callStack.size > 1
