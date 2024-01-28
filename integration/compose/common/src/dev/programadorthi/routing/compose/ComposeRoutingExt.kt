package dev.programadorthi.routing.compose

import dev.programadorthi.routing.core.Routing

public fun Routing.canPop(): Boolean = callStack.size > 1

public fun Routing.pop(result: Any? = null) {
    if (!canPop()) return

    poppedCall = callStack.removeLastOrNull()
    poppedCall?.popped = true
    poppedCall?.popResult = result
}
