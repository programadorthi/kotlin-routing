package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.voyager.history.VoyagerHistoryMode
import dev.programadorthi.routing.voyager.history.historyMode
import dev.programadorthi.routing.voyager.history.popWindowHistory

internal actual fun Routing.popOnPlatform(
    result: Any?,
    fallback: () -> Unit,
) {
    when (historyMode) {
        VoyagerHistoryMode.Memory -> fallback()
        else -> popWindowHistory()
    }
}

public actual val Routing.canPop: Boolean
    get() = historyMode != VoyagerHistoryMode.Memory || voyagerNavigator.canPop
