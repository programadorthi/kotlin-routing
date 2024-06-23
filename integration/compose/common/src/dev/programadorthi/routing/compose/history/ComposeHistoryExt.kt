package dev.programadorthi.routing.compose.history

import androidx.compose.runtime.Composable
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall

internal expect suspend fun ApplicationCall.platformPush(
    routing: Routing,
    fallback: () -> Unit,
)

internal expect suspend fun ApplicationCall.platformReplace(
    routing: Routing,
    fallback: () -> Unit,
)

internal expect suspend fun ApplicationCall.platformReplaceAll(
    routing: Routing,
    fallback: () -> Unit,
)

internal expect fun ApplicationCall.shouldNeglect(): Boolean

@Composable
internal expect fun Routing.restoreState(startUri: String)
