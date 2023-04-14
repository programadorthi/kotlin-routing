package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class VoyagerEventManager(
    override val coroutineContext: CoroutineContext,
    val initialUri: String,
) : CoroutineScope {
    val navigation = MutableStateFlow<VoyagerRouteEvent>(VoyagerRouteEvent.Idle)

    fun clearEvent() {
        launch {
            navigation.emit(value = VoyagerRouteEvent.Idle)
        }
    }

    // TODO: popUntil support

    fun pop() {
        launch {
            navigation.emit(VoyagerRouteEvent.Pop)
        }
    }

    fun replace(screen: Screen, replaceAll: Boolean) {
        launch {
            navigation.emit(VoyagerRouteEvent.Replace(screen, replaceAll))
        }
    }

    fun push(screen: Screen) {
        launch {
            navigation.emit(VoyagerRouteEvent.Push(screen))
        }
    }
}
