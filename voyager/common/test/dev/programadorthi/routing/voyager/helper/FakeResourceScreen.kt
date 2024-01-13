package dev.programadorthi.routing.voyager.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import dev.programadorthi.routing.voyager.VoyagerRoutingPopResult
import io.ktor.resources.Resource
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

@Serializable
data class VoyagerResult(
    val content: String,
)

@Resource("/fakescreen")
class FakeResourceScreen<T> : Screen, VoyagerRoutingPopResult<T> {
    var composed = ""
        private set

    var content = ""

    var disposed = false
        private set

    @Transient
    var parameters: T? = null
        private set

    override val key: ScreenKey = Random.Default.nextLong().toString()

    override fun onResult(result: T?) {
        parameters = result
    }

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
