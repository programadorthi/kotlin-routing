package dev.programadorthi.routing.compose

import dev.programadorthi.routing.core.Routing

public fun Routing.canPop(): Boolean = contentList.size > 1

public fun Routing.pop(result: Any? = null) {
    popResult = result
    contentList.removeLastOrNull()
}

@Suppress("UNCHECKED_CAST")
public fun <T> Routing.popResult(): T? = popResult as? T

public fun <T> Routing.popResult(default: T): T = popResult() ?: default

public fun <T> Routing.popResult(default: () -> T): T = popResult() ?: default()
