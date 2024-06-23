package dev.programadorthi.routing.compose.history

import dev.programadorthi.routing.compose.toSerializableType
import dev.programadorthi.routing.core.application.ApplicationCall
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun <T> ApplicationCall.serialize(): T {
    val state = toHistoryState()
    val encoded = Json.encodeToString(state)
    return encoded.toSerializableType()
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
