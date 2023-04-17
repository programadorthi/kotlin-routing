package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import dev.programadorthi.routing.core.application.Application
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal class VoyagerNavigatorManager(
    private val application: Application,
    val initialUri: String,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = application.coroutineContext

    internal var navigator: Navigator? = null

    fun pop() {
        val nav = navigator ?: navigator?.parent ?: return
        nav.pop()
    }

    fun popUntil(predicate: (Screen) -> Boolean) {
        val nav = navigator ?: navigator?.parent ?: return
        nav.popUntil(predicate)
    }

    fun push(screen: Screen) {
        val nav = navigator ?: navigator?.parent ?: return
        nav.push(screen)
    }

    fun replace(screen: Screen, replaceAll: Boolean) {
        val nav = navigator ?: navigator?.parent ?: return
        when {
            replaceAll -> nav.replaceAll(screen)
            else -> nav.replace(screen)
        }
    }
}
