package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall

public inline fun <reified T : Any> Routing.push(resource: T) {
    val destination = application.href(resource)
    execute(
        ApplicationCall(
            application = application,
            uri = destination,
            routeMethod = StackRouteMethod.Push,
        )
    )
}

public inline fun <reified T : Any> Routing.replace(resource: T) {
    val destination = application.href(resource)
    execute(
        ApplicationCall(
            application = application,
            uri = destination,
            routeMethod = StackRouteMethod.Replace,
        )
    )
}

public inline fun <reified T : Any> Routing.replaceAll(resource: T) {
    val destination = application.href(resource)
    execute(
        ApplicationCall(
            application = application,
            uri = destination,
            routeMethod = StackRouteMethod.ReplaceAll,
        )
    )
}
