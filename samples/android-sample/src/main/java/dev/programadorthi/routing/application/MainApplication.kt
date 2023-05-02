package dev.programadorthi.routing.application

import android.app.Application
import android.content.Context
import dev.programadorthi.routing.android.AndroidNavigator
import dev.programadorthi.routing.android.handle
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing

val Context.router: Routing
    get() = (applicationContext as MainApplication).router

class MainApplication : Application() {
    // You can create a router anywhere
    // I am creating here to routing to main activity wherever I can
    // WARNING: Using by lazy to avoid getting a null Context
    val router by lazy {
        routing {
            install(AndroidNavigator) {
                context = this@MainApplication
            }

            // Handle all the events to MainActivity
            handle<MainActivity>()
        }
    }
}