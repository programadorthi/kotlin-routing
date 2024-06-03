package dev.programadorthi.routing.compose.history

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.parametersOf
import io.ktor.util.toMap
import kotlinx.serialization.Serializable

@Serializable
internal data class ComposeHistoryState(
    val routeMethod: String,
    val name: String,
    val uri: String,
    val parameters: Map<String, List<String>>,
)

internal fun ComposeHistoryState.toCall(application: Application): ApplicationCall {
    val call =
        ApplicationCall(
            application = application,
            name = name,
            uri = uri,
            routeMethod = RouteMethod.parse(routeMethod),
            parameters = parametersOf(parameters),
        )
    call.restored = true
    return call
}

internal fun ApplicationCall.toHistoryState(): ComposeHistoryState {
    return ComposeHistoryState(
        routeMethod = routeMethod.value,
        name = name,
        uri = uri,
        parameters = parameters.toMap(),
    )
}
