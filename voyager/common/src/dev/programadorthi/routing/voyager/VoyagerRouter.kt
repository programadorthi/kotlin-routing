package dev.programadorthi.routing.voyager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application

@Composable
public fun VoyagerRouter(
    router: Routing,
    initialScreen: Screen = VoyagerEmptyScreen(),
) {
    val manager = remember(router) {
        router.application.voyagerNavigatorManager
    }

    CompositionLocalProvider(VoyagerLocalRouter provides router) {
        Navigator(initialScreen) { navigator ->
            SideEffect {
                manager.navigator = navigator
            }

            CurrentScreen()
        }
    }
}
