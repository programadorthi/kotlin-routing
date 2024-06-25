package dev.programadorthi.routing.voyager.history

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall

internal expect suspend fun ApplicationCall.platformPush(
    routing: Routing,
    body: suspend () -> Screen,
    fallback: suspend () -> Unit,
)

internal expect suspend fun ApplicationCall.platformReplace(
    routing: Routing,
    body: suspend () -> Screen,
    fallback: suspend () -> Unit,
)

internal expect suspend fun ApplicationCall.platformReplaceAll(
    routing: Routing,
    body: suspend () -> Screen,
    fallback: suspend () -> Unit,
)

internal expect fun ApplicationCall.shouldNeglect(): Boolean

@Composable
internal expect fun Routing.restoreState(onState: (Any) -> Unit)
