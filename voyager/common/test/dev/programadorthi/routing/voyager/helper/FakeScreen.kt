package dev.programadorthi.routing.voyager.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import kotlin.random.Random

class FakeScreen : Screen {

    var composed = ""
        private set

    var content = ""

    var disposed = false
        private set

    override val key: ScreenKey = Random.Default.nextLong().toString()

    @Composable
    override fun Content() {
        composed = content
        DisposableEffect(Unit) {
            onDispose {
                disposed = true
            }
        }
    }
}
