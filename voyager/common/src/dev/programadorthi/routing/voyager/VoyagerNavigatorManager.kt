package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import dev.programadorthi.routing.core.application.Application
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

public class VoyagerNavigatorManager(
    private val application: Application,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = application.coroutineContext

    internal var navigator: Navigator? = null

    public fun pop() {
        val nav = navigator ?: navigator?.parent ?: return
        nav.pop()
    }

    public fun popUntil(predicate: (Screen) -> Boolean) {
        val nav = navigator ?: navigator?.parent ?: return
        nav.popUntil(predicate)
    }

    public fun push(screen: Screen) {
        val nav = navigator ?: navigator?.parent ?: return
        nav.push(screen)
    }

    public fun replace(screen: Screen, replaceAll: Boolean) {
        val nav = navigator ?: navigator?.parent ?: return
        when {
            replaceAll -> nav.replaceAll(screen)
            else -> nav.replace(screen)
        }
    }
}
