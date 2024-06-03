package dev.programadorthi.routing.compose.history

import dev.programadorthi.routing.core.application.ApplicationCall
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun ApplicationCall.serialize(): String {
    val state = toHistoryState()
    return Json.encodeToString(state)
}

internal fun Any?.deserialize(): ComposeHistoryState? =
    when (this) {
        is String -> toState()
        else -> null
    }

private fun String.toState(): ComposeHistoryState? =
    runCatching {
        Json.decodeFromString<ComposeHistoryState>(this)
    }.getOrNull()
