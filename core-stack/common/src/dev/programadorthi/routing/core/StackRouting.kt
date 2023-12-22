package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters

public fun Routing.pop(
    parameters: Parameters = Parameters.Empty
) {
    application.checkPluginInstalled()
    val toPop = application.stackManager.toPop() ?: return
    // Notify the route that it was popped
    call(
        name = toPop.name,
        uri = toPop.uri,
        parameters = parameters,
        routeMethod = StackRouteMethod.Pop,
    )
    val toShow = application.stackManager.lastOrNull() ?: return
    // Notify the previous route with new parameters
    execute(
        ApplicationCall(
            application = application,
            name = toShow.name,
            uri = toShow.uri,
            parameters = parameters,
            routeMethod = toShow.routeMethod,
        ).toNeglect(neglect = true)
    )
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

private fun ApplicationCall.toNeglect(neglect: Boolean): ApplicationCall {
    this.neglect = neglect
    return this
}
