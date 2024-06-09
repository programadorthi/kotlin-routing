package dev.programadorthi.routing.voyager.history

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.voyager.voyagerNavigator

internal actual fun ApplicationCall.shouldNeglect(): Boolean = false

internal actual suspend fun ApplicationCall.platformPush(
    routing: Routing,
    body: suspend () -> Screen,
) {
    voyagerNavigator.push(body())
}

internal actual suspend fun ApplicationCall.platformReplace(
    routing: Routing,
    body: suspend () -> Screen,
) {
    voyagerNavigator.replace(body())
}

internal actual suspend fun ApplicationCall.platformReplaceAll(
    routing: Routing,
    body: suspend () -> Screen,
) {
    voyagerNavigator.replaceAll(body())
}

@Composable
internal actual fun Routing.restoreState(onState: (Any) -> Unit) {
    // No-op
    // Voyager has its internal state restoration
}
