package dev.programadorthi.routing.voyager.routes

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.voyager.screen
import dev.programadorthi.routing.voyager.screens.CartScreen
import dev.programadorthi.routing.voyager.screens.CatalogScreen

// Unregister is required for loaded named routes only
// This avoids throw an exception when loading routes with same name
fun Routing.unLoadRoutes() {
    unregisterNamed(name = "cart")
    unregisterNamed(name = "catalog")
}

fun Route.loadRoutes() {
    route(path = "/catalog", name = "catalog") {
        // Screen to show when routing /catalog
        screen {
            CatalogScreen()
        }

        // Nested route to /catalog/cart
        screen(path = "cart", name = "cart") {
            CartScreen()
        }
    }
}