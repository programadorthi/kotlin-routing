import androidx.compose.ui.window.Window
import dev.programadorthi.routing.voyager.SampleApplication
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        Window("Voyager Multiplatform Routing") {
            SampleApplication()
        }
    }
}
