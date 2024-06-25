package dev.programadorthi.routing.voyager.history

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall

internal actual fun ApplicationCall.shouldNeglect(): Boolean = false

internal actual suspend fun ApplicationCall.platformPush(
    routing: Routing,
    body: suspend () -> Screen,
    fallback: suspend () -> Unit,
) {
    fallback()
}

internal actual suspend fun ApplicationCall.platformReplace(
    routing: Routing,
    body: suspend () -> Screen,
    fallback: suspend () -> Unit,
) {
    fallback()
}

internal actual suspend fun ApplicationCall.platformReplaceAll(
    routing: Routing,
    body: suspend () -> Screen,
    fallback: suspend () -> Unit,
) {
    fallback()
}

@Composable
internal actual fun Routing.restoreState(onState: (Any) -> Unit) {
    // No-op
    // Voyager has its internal state restoration
}
