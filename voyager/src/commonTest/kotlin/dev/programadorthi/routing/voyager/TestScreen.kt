package dev.programadorthi.routing.voyager

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

class TestScreen(val value: Any) : Screen {
    private var contentCalled = false

    @Composable
    override fun Content() {
        contentCalled = true
    }
}
