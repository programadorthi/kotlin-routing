package dev.programadorthi.routing.android

import android.app.Activity
import android.content.Intent
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.resources.handle
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.KtorDsl

@KtorDsl
public fun Route.activity(
    path: String,
    name: String? = null,
    body: PipelineContext<Unit, ApplicationCall>.() -> Intent,
): Route = route(path = path, name = name) { activity(body) }

@KtorDsl
public fun Route.activity(
    path: String,
    method: RouteMethod,
    name: String? = null,
    body: PipelineContext<Unit, ApplicationCall>.() -> Intent,
): Route = route(path = path, name = name, method = method) { activity(body) }

@KtorDsl
public fun Route.activity(body: PipelineContext<Unit, ApplicationCall>.() -> Intent) {
    handle {
        call.activityManager.start(this, body(this))
    }
}

@KtorDsl
public inline fun <reified A : Activity> Route.activity(
    path: String,
    name: String? = null,
    crossinline body: PipelineContext<Unit, ApplicationCall>.(Intent) -> Unit = {},
): Route = route(path = path, name = name) { activity<A>(body) }

@KtorDsl
public inline fun <reified A : Activity> Route.activity(
    path: String,
    method: RouteMethod,
    name: String? = null,
    crossinline body: PipelineContext<Unit, ApplicationCall>.(Intent) -> Unit = {},
): Route = route(path = path, name = name, method = method) { activity<A>(body) }

@KtorDsl
public inline fun <reified A : Activity> Route.activity(crossinline body: PipelineContext<Unit, ApplicationCall>.(Intent) -> Unit = {}) {
    handle {
        val intent = Intent(call.currentActivity, A::class.java)
        body(this, intent)
        call.activityManager.start(this, intent)
    }
}

@KtorDsl
public inline fun <reified T : Any> Route.activity(noinline body: PipelineContext<Unit, ApplicationCall>.(T) -> Intent): Route {
    return handle<T> { resource ->
        call.activityManager.start(this, body(this, resource))
    }
}

@KtorDsl
public inline fun <reified T : Any> Route.activity(
    method: RouteMethod,
    noinline body: PipelineContext<Unit, ApplicationCall>.(T) -> Intent,
): Route {
    return handle<T>(method = method) { resource ->
        call.activityManager.start(this, body(this, resource))
    }
}

@KtorDsl
public inline fun <reified T : Any, reified A : Activity> Route.activity(
    noinline body: PipelineContext<Unit, ApplicationCall>.(T, Intent) -> Unit = { _, _ -> },
): Route {
    return handle<T> { resource ->
        val intent = Intent(call.currentActivity, A::class.java)
        body(this, resource, intent)
        call.activityManager.start(this, intent)
    }
}

@KtorDsl
public inline fun <reified T : Any, reified A : Activity> Route.activity(
    method: RouteMethod,
    noinline body: PipelineContext<Unit, ApplicationCall>.(T, Intent) -> Unit = { _, _ -> },
): Route {
    return handle<T>(method = method) { resource ->
        val intent = Intent(call.currentActivity, A::class.java)
        body(this, resource, intent)
        call.activityManager.start(this, intent)
    }
}
