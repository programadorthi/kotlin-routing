package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import kotlinx.coroutines.launch

public fun Routing.pop(
    parameters: Parameters = Parameters.Empty
) {
    with(application) {
        checkPluginInstalled()
        launch {
            val toPop = stackManager.toPop() ?: return@launch
            // Notify the route that it was popped
            this@pop.call(
                name = toPop.name,
                uri = toPop.uri,
                parameters = parameters,
                routeMethod = StackRouteMethod.Pop,
            )
        }
    }
}

public fun Routing.push(
    path: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    application.checkPluginInstalled()
    execute(
        ApplicationCall(
            application = application,
            uri = path,
            parameters = parameters,
            routeMethod = StackRouteMethod.Push,
        ).toNeglect(neglect)
    )
}

public fun Routing.pushNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    application.checkPluginInstalled()
    execute(
        ApplicationCall(
            application = application,
            name = name,
            parameters = parameters,
            routeMethod = StackRouteMethod.Push,
        ).toNeglect(neglect)
    )
}

public fun Routing.replace(
    path: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    application.checkPluginInstalled()
    execute(
        ApplicationCall(
            application = application,
            uri = path,
            parameters = parameters,
            routeMethod = StackRouteMethod.Replace,
        ).toNeglect(neglect)
    )
}

public fun Routing.replaceAll(
    path: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    application.checkPluginInstalled()
    execute(
        ApplicationCall(
            application = application,
            uri = path,
            parameters = parameters,
            routeMethod = StackRouteMethod.ReplaceAll,
        ).toNeglect(neglect)
    )
}

public fun Routing.replaceNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    application.checkPluginInstalled()
    execute(
        ApplicationCall(
            application = application,
            name = name,
            parameters = parameters,
            routeMethod = StackRouteMethod.Replace,
        ).toNeglect(neglect)
    )
}

public fun Routing.replaceAllNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    application.checkPluginInstalled()
    execute(
        ApplicationCall(
            application = application,
            name = name,
            parameters = parameters,
            routeMethod = StackRouteMethod.ReplaceAll,
        ).toNeglect(neglect)
    )
}

internal fun ApplicationCall.toNeglect(neglect: Boolean): ApplicationCall {
    this.neglect = neglect
    return this
}
