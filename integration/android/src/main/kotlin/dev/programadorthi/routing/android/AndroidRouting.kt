package dev.programadorthi.routing.android

import android.os.Bundle
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.util.Attributes

public fun Routing.pushActivity(
    name: String = "",
    path: String = "",
    requestCode: Int? = null,
    activityOptions: Bundle? = null,
    attributes: Attributes = Attributes(),
    parameters: Parameters = Parameters.Empty,
) {
    execute(
        routeMethod = RouteMethod.Push,
        name = name,
        path = path,
        requestCode = requestCode,
        activityOptions = activityOptions,
        attributes = attributes,
        parameters = parameters,
    )
}

public fun Routing.replaceActivity(
    name: String = "",
    path: String = "",
    requestCode: Int? = null,
    activityOptions: Bundle? = null,
    attributes: Attributes = Attributes(),
    parameters: Parameters = Parameters.Empty,
) {
    execute(
        routeMethod = RouteMethod.Replace,
        name = name,
        path = path,
        requestCode = requestCode,
        activityOptions = activityOptions,
        attributes = attributes,
        parameters = parameters,
    )
}

public fun Routing.replaceAllActivity(
    name: String = "",
    path: String = "",
    requestCode: Int? = null,
    activityOptions: Bundle? = null,
    attributes: Attributes = Attributes(),
    parameters: Parameters = Parameters.Empty,
) {
    execute(
        routeMethod = RouteMethod.ReplaceAll,
        name = name,
        path = path,
        requestCode = requestCode,
        activityOptions = activityOptions,
        attributes = attributes,
        parameters = parameters,
    )
}

private fun Routing.execute(
    routeMethod: RouteMethod,
    name: String = "",
    path: String = "",
    requestCode: Int?,
    activityOptions: Bundle?,
    attributes: Attributes = Attributes(),
    parameters: Parameters = Parameters.Empty,
) {
    val call =
        ApplicationCall(
            application = application,
            name = name,
            uri = path,
            routeMethod = routeMethod,
            attributes = attributes,
            parameters = parameters,
        )
    call.requestCode = requestCode
    call.activityOptions = activityOptions
    execute(call)
}
