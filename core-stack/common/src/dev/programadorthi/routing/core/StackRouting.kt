package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters

public fun Routing.pop(
    parameters: Parameters = Parameters.Empty
) {
    application.checkPluginInstalled()
    val toPop = application.stackManager.toPop() ?: return
    execute(
        ApplicationCall(
            application = application,
            name = toPop.name,
            uri = toPop.uri,
            parameters = parameters,
            routeMethod = StackRouteMethod.Pop,
        )
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
        ).tryNeglect(neglect)
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
        ).tryNeglect(neglect)
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
        ).tryNeglect(neglect)
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
        ).tryNeglect(neglect)
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
        ).tryNeglect(neglect)
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
        ).tryNeglect(neglect)
    )
}

private fun ApplicationCall.tryNeglect(neglect: Boolean): ApplicationCall {
    stackNeglect = neglect
    return this
}
