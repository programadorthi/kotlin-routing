package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.toMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Serializable
internal data class StackState(
    val name: String,
    val routeMethod: String,
    val uri: String,
    val parameters: Map<String, List<String>>,
)

internal fun StackState.toJson(): String = Json.encodeToString(serializer(), this)

internal fun String.toState(): StackState = Json.decodeFromString(serializer(), this)

internal fun List<StackState>.toJson(): String = Json.encodeToString(serializer(), this)

internal fun String.toStateList(): List<StackState> = Json.decodeFromString(serializer(), this)

internal fun ApplicationCall.toState(): StackState = StackState(
    name = this.name,
    routeMethod = this.routeMethod.value,
    uri = this.uri,
    parameters = this.parameters.toMap(),
)
