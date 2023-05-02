package dev.programadorthi.routing.android

import android.app.Activity
import android.content.Intent
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.pluginOrNull
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll
import dev.programadorthi.routing.core.route
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor

@KtorDsl
public inline fun <reified T : Activity> Route.handle(): Route =
    handle<T>(path = "${T::class.qualifiedName}", name = null) {}

@KtorDsl
public inline fun <reified T : Activity> Route.handle(
    path: String,
    name: String? = null,
): Route = handle<T>(path = path, name = name) {}

@KtorDsl
public inline fun <reified T : Activity> Route.handle(
    path: String,
    name: String? = null,
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(Intent?) -> Unit
): Route = route(path = path, name = name) { handle<T>(body) }

@KtorDsl
public inline fun <reified T : Activity> Route.handle(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(Intent?) -> Unit,
): Route {
    checkPluginInstalled()

    push<T>(body = body)
    replace<T>(body = body)
    replaceAll<T>(body = body)
    pop<T> {
        body(this, null)
    }

    return this
}

@KtorDsl
public inline fun <reified T : Activity> Route.push(): Route =
    push<T>(path = "${T::class.qualifiedName}", name = null) {}

@KtorDsl
public inline fun <reified T : Activity> Route.push(
    path: String,
    name: String? = null,
): Route = push<T>(path = path, name = name) {}

@KtorDsl
public inline fun <reified T : Activity> Route.push(
    path: String,
    name: String? = null,
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(Intent) -> Unit
): Route = route(path = path, name = name) { push<T>(body) }

@KtorDsl
public inline fun <reified T : Activity> Route.push(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(Intent) -> Unit
): Route {
    checkPluginInstalled()

    push {
        call.androidNavigatorManager.navigate(
            clazz = T::class.java,
            routeMethod = StackRouteMethod.Push,
            parameters = call.parameters,
        ) { intent ->
            body(this, intent)
        }
    }

    application.androidNavigatorManager.register(
        clazz = T::class,
        routeMethod = StackRouteMethod.Push,
        route = this,
    )

    return this
}

@KtorDsl
public inline fun <reified T : Activity> Route.replace(): Route =
    replace<T>(path = "${T::class.qualifiedName}", name = null) {}

@KtorDsl
public inline fun <reified T : Activity> Route.replace(
    path: String,
    name: String? = null,
): Route = replace<T>(path = path, name = name) {}

@KtorDsl
public inline fun <reified T : Activity> Route.replace(
    path: String,
    name: String? = null,
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(Intent) -> Unit
): Route = route(path = path, name = name) { replace<T>(body) }

@KtorDsl
public inline fun <reified T : Activity> Route.replace(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(Intent) -> Unit
): Route {
    checkPluginInstalled()

    replace {
        call.androidNavigatorManager.navigate(
            clazz = T::class.java,
            routeMethod = StackRouteMethod.Replace,
            parameters = call.parameters,
        ) { intent ->
            body(this, intent)
        }
    }

    application.androidNavigatorManager.register(
        clazz = T::class,
        routeMethod = StackRouteMethod.Replace,
        route = this,
    )

    return this
}

@KtorDsl
public inline fun <reified T : Activity> Route.replaceAll(): Route =
    replaceAll<T>(path = "${T::class.qualifiedName}", name = null) {}

@KtorDsl
public inline fun <reified T : Activity> Route.replaceAll(
    path: String,
    name: String? = null,
): Route = replaceAll<T>(path = path, name = name) {}

@KtorDsl
public inline fun <reified T : Activity> Route.replaceAll(
    path: String,
    name: String? = null,
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(Intent) -> Unit
): Route = route(path = path, name = name) { replaceAll<T>(body) }

@KtorDsl
public inline fun <reified T : Activity> Route.replaceAll(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(Intent) -> Unit
): Route {
    checkPluginInstalled()

    replaceAll {
        call.androidNavigatorManager.navigate(
            clazz = T::class.java,
            routeMethod = StackRouteMethod.ReplaceAll,
            parameters = call.parameters,
        ) { intent ->
            body(this, intent)
        }
    }

    application.androidNavigatorManager.register(
        clazz = T::class,
        routeMethod = StackRouteMethod.ReplaceAll,
        route = this,
    )

    return this
}

@KtorDsl
public inline fun <reified T : Activity> Route.pop(): Route =
    pop<T>(path = "${T::class.qualifiedName}") {}

@KtorDsl
public inline fun <reified T : Activity> Route.pop(
    path: String,
): Route = pop<T>(path = path) {}

@KtorDsl
public inline fun <reified T : Activity> Route.pop(
    path: String,
    noinline body: PipelineInterceptor<Unit, ApplicationCall>
): Route = route(path = path) { pop<T>(body) }

@KtorDsl
public inline fun <reified T : Activity> Route.pop(
    noinline body: PipelineInterceptor<Unit, ApplicationCall>
): Route {
    checkPluginInstalled()

    pop {
        call.androidNavigatorManager.navigate(
            clazz = T::class.java,
            routeMethod = StackRouteMethod.Pop,
            parameters = call.parameters,
        ) {
            body(this, Unit)
        }
    }

    application.androidNavigatorManager.register(
        clazz = T::class,
        routeMethod = StackRouteMethod.Pop,
        route = this,
    )

    return this
}


public fun Route.checkPluginInstalled() {
    checkNotNull(application.pluginOrNull(AndroidNavigator)) {
        "AndroidNavigator plugin not installed"
    }
}
