package dev.programadorthi.routing.voyager.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import dev.programadorthi.routing.voyager.VoyagerRoutingPopResult
import io.ktor.http.Parameters
import io.ktor.resources.Resource
import kotlinx.serialization.Transient
import kotlin.random.Random

@Resource("/fakescreen")
class FakeScreen : Screen, VoyagerRoutingPopResult<Parameters> {

    var composed = ""
        private set

    var content = ""

    var disposed = false
        private set

    @Transient
    var parameters = Parameters.Empty
        private set

    override val key: ScreenKey = Random.Default.nextLong().toString()

    override fun onResult(result: Parameters) {
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
