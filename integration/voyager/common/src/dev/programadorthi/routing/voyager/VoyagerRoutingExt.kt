package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import dev.programadorthi.routing.core.Routing

internal expect fun Routing.popOnPlatform(
    result: Any? = null,
    fallback: () -> Unit,
)

public expect val Routing.canPop: Boolean

public fun Routing.canPop(): Boolean = canPop

public fun Routing.pop(result: Any? = null) {
    popOnPlatform(result) {
        val navigator = voyagerNavigator
        if (navigator.pop()) {
            navigator.trySendPopResult(result)
        }
    }
}

public fun Routing.popUntil(
    result: Any? = null,
    predicate: (Screen) -> Boolean,
) {
    val navigator = voyagerNavigator
    if (navigator.popUntil(predicate)) {
        navigator.trySendPopResult(result)
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> Navigator.trySendPopResult(result: T?) {
    if (result == null) return
    // Safe here because navigator pop() call is sync by default and last item is the previous screen
    val currentScreen = lastItemOrNull as? VoyagerRoutingPopResult<T>
    currentScreen?.onResult(result)
}
