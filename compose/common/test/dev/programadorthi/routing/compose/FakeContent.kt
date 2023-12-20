package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable

internal class FakeContent {
    var content = ""

    var result = ""
        private set

    @Composable
    fun Composable() {
        result = content
    }
}
