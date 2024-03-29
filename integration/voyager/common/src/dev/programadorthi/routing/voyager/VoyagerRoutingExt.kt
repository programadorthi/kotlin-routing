package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application

public fun Routing.canPop(): Boolean = application.voyagerNavigator.canPop

public fun Routing.pop(result: Any? = null) {
    val navigator = application.voyagerNavigator
    if (navigator.pop()) {
        navigator.trySendPopResult(result)
    }
}

public fun Routing.popUntil(
    result: Any? = null,
    predicate: (Screen) -> Boolean,
) {
    val navigator = application.voyagerNavigator
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
