package dev.programadorthi.routing.compose

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.programadorthi.routing.core.Routing

public val LocalPopResult: ProvidableCompositionLocal<ComposeRoutingPopResult?> =
    staticCompositionLocalOf { null }

public fun Routing.canPop(): Boolean = contentList.size > 1

public fun Routing.pop(result: Any? = null) {
    contentList.removeLastOrNull()
    if (result == null) return

    val lastIndex = contentList.lastIndex
    if (lastIndex < 0) return

    val lastContent = contentList[lastIndex]
    val popResult = ComposeRoutingPopResult(content = result)

    contentList[lastIndex] = {
        CompositionLocalProvider(LocalPopResult provides popResult) {
            lastContent()
        }
    }
}
