package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.parametersOf
import io.ktor.util.toMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Serializable
private data class StackState(
    val name: String,
    val routeMethod: String,
    val uri: String,
    val parameters: Map<String, List<String>>,
)

private fun StackState.toJson(): String = Json.encodeToString(serializer(), this)

private fun String.toState(): StackState = Json.decodeFromString(serializer(), this)

private fun List<StackState>.toJson(): String = Json.encodeToString(serializer(), this)

private fun String.toStateList(): List<StackState> = Json.decodeFromString(serializer(), this)

private fun ApplicationCall.toState(): StackState =
    StackState(
        name = this.name,
        routeMethod = this.routeMethod.value,
        uri = this.uri,
        parameters = this.parameters.toMap(),
    )

internal fun ApplicationCall.toData(): String = toState().toJson()

internal fun String.toCall(application: Application): ApplicationCall {
    val stackState = toState()
    return ApplicationCall(
        application = application,
        name = stackState.name,
        uri = stackState.uri,
        routeMethod = RouteMethod(stackState.routeMethod),
        parameters = parametersOf(stackState.parameters),
    )
}
