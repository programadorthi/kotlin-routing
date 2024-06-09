package dev.programadorthi.routing.voyager.history

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.parametersOf
import io.ktor.util.toMap
import kotlinx.serialization.Serializable

@Serializable
internal data class VoyagerHistoryState(
    val routeMethod: String,
    val name: String,
    val uri: String,
    val parameters: Map<String, List<String>>,
)

internal fun VoyagerHistoryState.toCall(application: Application): ApplicationCall {
    return ApplicationCall(
        application = application,
        name = name,
        uri = uri,
        routeMethod = RouteMethod.parse(routeMethod),
        parameters = parametersOf(parameters),
    )
}

internal fun ApplicationCall.toHistoryState(): VoyagerHistoryState {
    return VoyagerHistoryState(
        routeMethod = routeMethod.value,
        name = name,
        uri = uri,
        parameters = parameters.toMap(),
    )
}
