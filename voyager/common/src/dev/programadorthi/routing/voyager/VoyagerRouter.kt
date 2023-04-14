package dev.programadorthi.routing.voyager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        router.application.voyagerEventManager
    }

    CompositionLocalProvider(VoyagerLocalRouter provides router) {
        Navigator(initialScreen) { navigator ->
            val event by manager.navigation.collectAsState()

            LaunchedEffect(key1 = event) {
                event.execute(navigator)
                if (event !is VoyagerRouteEvent.Idle) {
                    manager.clearEvent()
                }
            }

            CurrentScreen()
        }
    }
}
