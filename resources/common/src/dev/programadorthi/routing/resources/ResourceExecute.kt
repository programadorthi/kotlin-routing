package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.pluginOrNull
import dev.programadorthi.routing.core.application.redirectToPath
import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.callWithBody

public inline fun <reified T : Any> Routing.call(
    resource: T,
    routeMethod: RouteMethod = RouteMethod.Empty,
) {
    checkNotNull(application.pluginOrNull(Resources)) {
        "Resources plugin not installed"
    }
    call(uri = application.href(resource), routeMethod = routeMethod)
}

public inline fun <reified T : Any, B : Any> Routing.callWithBody(
    resource: T,
    body: B,
    routeMethod: RouteMethod = RouteMethod.Empty,
) {
    checkNotNull(application.pluginOrNull(Resources)) {
        "Resources plugin not installed"
    }
    callWithBody(
        body = body,
        uri = application.href(resource),
        routeMethod = routeMethod,
    )
}

public inline fun <reified T : Any> ApplicationCall.redirectTo(resource: T) {
    checkNotNull(application.pluginOrNull(Resources)) {
        "Resources plugin not installed"
    }
    redirectToPath(path = application.href(resource))
}

public inline fun <reified T : Any> ApplicationCall.redirectTo(
    resource: T,
    method: RouteMethod,
) {
    checkNotNull(application.pluginOrNull(Resources)) {
        "Resources plugin not installed"
    }
    redirectToPath(
        path = application.href(resource),
        method = method,
    )
}

public inline fun <reified T : Any> Routing.push(resource: T) {
    call(resource = resource, routeMethod = RouteMethod.Push)
}

public inline fun <reified T : Any> Routing.replace(resource: T) {
    call(resource = resource, routeMethod = RouteMethod.Replace)
}

public inline fun <reified T : Any> Routing.replaceAll(resource: T) {
    call(resource = resource, routeMethod = RouteMethod.ReplaceAll)
}
