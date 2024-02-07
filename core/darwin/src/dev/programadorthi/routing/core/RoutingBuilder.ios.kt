package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import io.ktor.utils.io.KtorDsl

@KtorDsl
public fun Route.route(
    path: String,
    build: Route.() -> Unit,
): Route = route(path = path, name = null, build = build)

@KtorDsl
public fun Route.route(
    path: String,
    method: RouteMethod,
    build: Route.() -> Unit,
): Route =
    route(
        path = path,
        method = method,
        name = null,
        build = build,
    )

@KtorDsl
public fun Route.handle(
    path: String,
    body: ApplicationCall.() -> Unit,
): Route = handle(path = path, name = null, body = body)

@KtorDsl
public fun Route.handle(
    path: String,
    name: String? = null,
    body: ApplicationCall.() -> Unit,
): Route =
    route(path, name) {
        handle {
            call.body()
        }
    }

@KtorDsl
public fun Route.handle(
    path: String,
    method: RouteMethod,
    body: ApplicationCall.() -> Unit,
): Route = handle(path = path, name = null, method = method, body = body)

@KtorDsl
public fun Route.handle(
    path: String,
    method: RouteMethod,
    name: String? = null,
    body: ApplicationCall.() -> Unit,
): Route =
    route(path = path, name = name, method = method) {
        handle {
            call.body()
        }
    }
