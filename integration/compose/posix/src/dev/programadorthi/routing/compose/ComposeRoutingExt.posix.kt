package dev.programadorthi.routing.compose

import dev.programadorthi.routing.core.Routing

internal actual fun Routing.popOnPlatform(
    result: Any?,
    fallback: () -> Unit,
) {
    fallback()
}
