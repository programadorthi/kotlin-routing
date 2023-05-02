package dev.programadorthi.routing.android

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.application
import kotlin.reflect.KClass

public fun Routing.unregisterActivityForAllMethod(clazz: KClass<*>, name: String? = null) {
    listOf(
        StackRouteMethod.Pop,
        StackRouteMethod.Push,
        StackRouteMethod.Replace,
        StackRouteMethod.ReplaceAll
    ).forEach {
        unregisterActivityForMethod(clazz, it)
    }

    if (name.isNullOrBlank()) return

    unregisterNamed(name)
}

public fun Routing.unregisterActivityForMethod(clazz: KClass<*>, routeMethod: StackRouteMethod) {
    application.androidNavigatorManager.unregister(clazz, routeMethod)
}