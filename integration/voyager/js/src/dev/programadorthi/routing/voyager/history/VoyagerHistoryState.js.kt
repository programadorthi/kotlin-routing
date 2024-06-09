package dev.programadorthi.routing.voyager.history

import dev.programadorthi.routing.core.application.ApplicationCall
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun ApplicationCall.serialize(): String {
    val state = toHistoryState()
    return Json.encodeToString(state)
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
