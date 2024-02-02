package dev.programadorthi.routing.android

import android.os.Bundle
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.resources.href

public inline fun <reified T : Any> Routing.pushActivity(
    resource: T,
    requestCode: Int? = null,
    activityOptions: Bundle? = null,
) {
    execute(
        resource = resource,
        routeMethod = RouteMethod.Push,
        requestCode = requestCode,
        activityOptions = activityOptions,
    )
}

public inline fun <reified T : Any> Routing.replaceActivity(
    resource: T,
    requestCode: Int? = null,
    activityOptions: Bundle? = null,
) {
    execute(
        resource = resource,
        routeMethod = RouteMethod.Replace,
        requestCode = requestCode,
        activityOptions = activityOptions,
    )
}

public inline fun <reified T : Any> Routing.replaceAllActivity(
    resource: T,
    requestCode: Int? = null,
    activityOptions: Bundle? = null,
) {
    execute(
        resource = resource,
        routeMethod = RouteMethod.ReplaceAll,
        requestCode = requestCode,
        activityOptions = activityOptions,
    )
}

@PublishedApi
internal inline fun <reified T : Any> Routing.execute(
    resource: T,
    routeMethod: RouteMethod,
    requestCode: Int?,
    activityOptions: Bundle?,
) {
    val call =
        ApplicationCall(
            application = application,
            uri = application.href(resource),
            routeMethod = routeMethod,
        )
    call.requestCode = requestCode
    call.activityOptions = activityOptions
    execute(call)
}
