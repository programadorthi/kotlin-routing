import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.toJson
import dev.programadorthi.routing.core.toState
import io.ktor.http.parametersOf

public fun ApplicationCall.toData(): String = toState().toJson()

public fun String.toCall(application: Application): ApplicationCall {
    val stackState = toState()
    return ApplicationCall(
        application = application,
        name = stackState.name,
        uri = stackState.uri,
        routeMethod = RouteMethod(stackState.routeMethod),
        parameters = parametersOf(stackState.parameters),
    )
}
