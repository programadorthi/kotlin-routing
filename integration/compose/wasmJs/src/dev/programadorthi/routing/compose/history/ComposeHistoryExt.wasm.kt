package dev.programadorthi.routing.compose.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.programadorthi.routing.compose.callStack
import dev.programadorthi.routing.compose.push
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall

internal actual fun ApplicationCall.shouldNeglect(): Boolean = false

internal actual suspend fun ApplicationCall.platformPush(routing: Routing) {
    routing.callStack.add(this)
}

internal actual suspend fun ApplicationCall.platformReplace(routing: Routing) {
    routing.callStack.run {
        removeLastOrNull()
        add(this@platformReplace)
    }
}

internal actual suspend fun ApplicationCall.platformReplaceAll(routing: Routing) {
    routing.callStack.run {
        clear()
        add(this@platformReplaceAll)
    }
}

@Composable
internal actual fun Routing.restoreState(startUri: String) {
    LaunchedEffect(Unit) {
        if (callStack.isEmpty()) {
            push(path = startUri)
        }
    }
}
