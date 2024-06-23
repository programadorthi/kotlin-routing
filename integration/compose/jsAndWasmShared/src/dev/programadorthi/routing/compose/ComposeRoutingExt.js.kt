package dev.programadorthi.routing.compose

import dev.programadorthi.routing.compose.history.ComposeHistoryMode
import dev.programadorthi.routing.compose.history.historyMode
import dev.programadorthi.routing.core.Routing
import kotlinx.browser.window

internal actual fun Routing.popOnPlatform(
    result: Any?,
    fallback: () -> Unit,
) {
    when (historyMode) {
        ComposeHistoryMode.Memory -> fallback()
        else -> {
            popResult = result
            window.history.go(-1)
        }
    }
}

public actual val Routing.canPop: Boolean
    get() = historyMode != ComposeHistoryMode.Memory || callStack.size > 1
