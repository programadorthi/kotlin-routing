package dev.programadorthi.routing.voyager

import androidx.compose.runtime.Composable
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.voyager.screens.HomeScreen
import dev.programadorthi.routing.voyager.screens.LoginScreen
import dev.programadorthi.routing.voyager.screens.LogoutScreen

val router = routing {
    install(VoyagerNavigator)

    screen(path = "/", name = "home") {
        HomeScreen()
    }

    screen(path = "/login", name = "login") {
        LoginScreen()
    }

    screen(path = "/logout", name = "logout") {
        LogoutScreen()
    }
}

@Composable
fun SampleApplication() {
    VoyagerRouter(router = router, initialScreen = HomeScreen())
}