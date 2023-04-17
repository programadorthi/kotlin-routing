import androidx.compose.ui.window.Window
import dev.programadorthi.routing.voyager.SampleApplication
import platform.AppKit.NSApp
import platform.AppKit.NSApplication

fun main() {
    NSApplication.sharedApplication()
    Window("VoyagerMultiplatformRouting") {
        SampleApplication()
    }
    NSApp?.run()
}
