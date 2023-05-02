package dev.programadorthi.routing.android

import android.app.Activity
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll
import io.ktor.http.Parameters

public fun Routing.popActivity() {
    application.androidNavigatorManager.finishCurrentActivity()
}

public inline fun <reified T : Activity> Routing.pushActivity(
    parameters: Parameters = Parameters.Empty
) {
    val destination = application.androidNavigatorManager.getRouteForActivity(
        clazz = T::class,
        routeMethod = StackRouteMethod.Push,
    )
    push(path = destination.toString(), parameters = parameters)
}

public inline fun <reified T : Activity> Routing.replaceActivity(
    parameters: Parameters = Parameters.Empty
) {
    val destination = application.androidNavigatorManager.getRouteForActivity(
        clazz = T::class,
        routeMethod = StackRouteMethod.Replace,
    )
    replace(path = destination.toString(), parameters = parameters)
}

public inline fun <reified T : Activity> Routing.replaceAllActivity(
    parameters: Parameters = Parameters.Empty
) {
    val destination = application.androidNavigatorManager.getRouteForActivity(
        clazz = T::class,
        routeMethod = StackRouteMethod.ReplaceAll,
    )
    replaceAll(path = destination.toString(), parameters = parameters)
}