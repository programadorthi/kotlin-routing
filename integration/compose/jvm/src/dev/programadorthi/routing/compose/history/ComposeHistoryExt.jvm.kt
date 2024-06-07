package dev.programadorthi.routing.compose.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import dev.programadorthi.routing.compose.callStack
import dev.programadorthi.routing.compose.push
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal actual fun ApplicationCall.shouldNeglect(): Boolean = restored

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
    val routingPath = remember(this) { toString() }
    val history =
        rememberSaveable(
            key = "state:restoration:$routingPath",
            init = { callStack.toList() },
            saver =
                Saver<List<ApplicationCall>, String>(
                    restore = { states ->
                        val result = Json.decodeFromString<List<ComposeHistoryState>>(states)
                        result.map { it.toCall(application) }
                    },
                    save = {
                        val calls = callStack.toList().map { it.toHistoryState() }
                        Json.encodeToString(calls)
                    },
                ),
        )
    LaunchedEffect(Unit) {
        val lastCall = history.lastOrNull()
        when {
            lastCall?.restored == true -> {
                callStack.clear()
                callStack.addAll(history)
                execute(lastCall)
            }

            else -> push(path = startUri)
        }
    }
}
