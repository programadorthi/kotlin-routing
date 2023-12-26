package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import io.ktor.util.pipeline.execute
import io.ktor.util.putAll
import kotlinx.coroutines.launch

public fun Routing.pop(
    parameters: Parameters = Parameters.Empty
) {
    with(application) {
        checkPluginInstalled()
        launch {
            val toPop = stackManager.toPop() ?: return@launch
            // Attributes instances are by call and we need a fresh one
            val attributes = Attributes()
            attributes.putAll(toPop.attributes)
            // Notify the route that it was popped
            val notify = ApplicationCall(
                application = toPop.application,
                name = toPop.name,
                uri = toPop.uri,
                attributes = attributes,
                parameters = parameters,
                routeMethod = StackRouteMethod.Pop,
            )
            notify.isForward = false
            this@with.execute(notify)
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
