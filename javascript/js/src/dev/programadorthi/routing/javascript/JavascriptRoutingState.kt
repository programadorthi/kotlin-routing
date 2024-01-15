package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.toMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
internal data class JavascriptRoutingState(
    val routeMethod: String,
    val name: String,
    val uri: String,
    val parameters: Map<String, List<String>>,
)

internal fun ApplicationCall.serialize(): String {
    val state =
        JavascriptRoutingState(
            routeMethod = routeMethod.value,
            name = name,
            uri = uri,
            parameters = parameters.toMap(),
        )
    return Json.encodeToString(state)
}

internal fun Any?.deserialize(): JavascriptRoutingState? =
    when (this) {
        is String -> Json.decodeFromString(this)
        else -> null
    }
