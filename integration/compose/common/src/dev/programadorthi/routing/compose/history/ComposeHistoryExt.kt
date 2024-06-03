package dev.programadorthi.routing.compose.history

import androidx.compose.runtime.Composable
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall

internal expect suspend fun ApplicationCall.platformPush(routing: Routing)

internal expect suspend fun ApplicationCall.platformReplace(routing: Routing)

internal expect suspend fun ApplicationCall.platformReplaceAll(routing: Routing)

internal expect fun ApplicationCall.shouldNeglect(): Boolean

@Composable
internal expect fun Routing.restoreState()
