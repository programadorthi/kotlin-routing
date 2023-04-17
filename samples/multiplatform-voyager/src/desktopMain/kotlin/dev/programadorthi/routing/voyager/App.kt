package dev.programadorthi.routing.voyager

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

public fun main() {
    application {
        Window(
            title = "Voyager Multiplatform Routing",
            onCloseRequest = ::exitApplication,
        ) {
            SampleApplication()
        }
    }
}