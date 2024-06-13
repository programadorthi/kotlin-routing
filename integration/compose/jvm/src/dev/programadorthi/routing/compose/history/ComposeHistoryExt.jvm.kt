package dev.programadorthi.routing.compose.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import dev.programadorthi.routing.compose.callStack
import dev.programadorthi.routing.compose.content
import dev.programadorthi.routing.compose.push
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.call
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    LaunchedEffect(routingPath) {
        when (history.lastOrNull()) {
            null -> push(path = startUri)
            else -> restoreLastCall(history)
        }
    }
}

private fun Routing.restoreLastCall(history: List<ApplicationCall>) {
    val lastCall = history.last()
    lastCall.restored = true
    val hasContent = lastCall.content != null
    callStack.clear()
    callStack.addAll(
        history.subList(
            fromIndex = 0,
            toIndex =
                if (hasContent) {
                    history.size
                } else {
                    history.lastIndex
                },
        ),
    )
    if (!hasContent) {
        call(
            name = lastCall.name,
            uri = lastCall.uri,
            parameters = lastCall.parameters,
            attributes = lastCall.attributes,
            routeMethod = RouteMethod.Push,
        )
    }
}
