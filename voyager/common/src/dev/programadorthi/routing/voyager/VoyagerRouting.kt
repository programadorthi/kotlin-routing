/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.method
import dev.programadorthi.routing.core.route
import io.ktor.util.KtorDsl

@KtorDsl
public fun Route.push(
    path: String,
    name: String? = null,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path, name = name) {
    push(body)
}

@KtorDsl
public fun Route.push(
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route {
    return method(RouteMethod.Push) {
        handle {
            val screen = body(this, Unit)
            call.voyagerEventManager.push(screen = screen)
        }
    }
}

@KtorDsl
public fun Route.replace(
    path: String,
    name: String? = null,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path, name = name) {
    replace(body)
}

@KtorDsl
public fun Route.replace(
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route {
    return method(RouteMethod.Replace) {
        handle {
            val screen = body(this, Unit)
            call.voyagerEventManager.replace(screen = screen, replaceAll = false)
        }
    }
}

@KtorDsl
public fun Route.replaceAll(
    path: String,
    name: String? = null,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path, name = name) {
    replaceAll(body)
}

@KtorDsl
public fun Route.replaceAll(
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route {
    return method(RouteMethod.ReplaceAll) {
        handle {
            val screen = body(this, Unit)
            call.voyagerEventManager.replace(screen = screen, replaceAll = true)
        }
    }
}

@KtorDsl
public fun Route.handle(
    path: String,
    name: String? = null,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path, name) {
    push(body)
    replace(body)
    replaceAll(body)
}
