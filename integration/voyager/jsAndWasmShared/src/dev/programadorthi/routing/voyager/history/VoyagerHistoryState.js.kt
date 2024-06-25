package dev.programadorthi.routing.voyager.history

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.voyager.toSerializableType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun <T> ApplicationCall.serialize(): T {
    val state = toHistoryState()
    val encoded = Json.encodeToString(state)
    return encoded.toSerializableType()
}

internal fun Any?.deserialize(): VoyagerHistoryState? =
    when (this) {
        is String -> toState()
        else -> null
    }

private fun String.toState(): VoyagerHistoryState? =
    runCatching {
        Json.decodeFromString<VoyagerHistoryState>(this)
    }.getOrNull()
