/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.pluginOrNull
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll
import dev.programadorthi.routing.core.route
import io.ktor.util.KtorDsl

@KtorDsl
public fun Route.pushScreen(
    path: String,
    name: String? = null,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path, name = name) {
    pushScreen(body)
}

@KtorDsl
public fun Route.pushScreen(
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route {
    checkPluginInstalled()
    return push {
        val screen = body(this, Unit)
        call.voyagerEventManager.push(screen = screen)
    }
}

@KtorDsl
public fun Route.replaceScreen(
    path: String,
    name: String? = null,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path, name = name) {
    replaceScreen(body)
}

@KtorDsl
public fun Route.replaceScreen(
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route {
    checkPluginInstalled()
    return replace {
        val screen = body(this, Unit)
        call.voyagerEventManager.replace(screen = screen, replaceAll = false)
    }
}

@KtorDsl
public fun Route.replaceAllScreen(
    path: String,
    name: String? = null,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path, name = name) {
    replaceAllScreen(body)
}

@KtorDsl
public fun Route.replaceAllScreen(
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route {
    checkPluginInstalled()
    return replaceAll {
        val screen = body(this, Unit)
        call.voyagerEventManager.replace(screen = screen, replaceAll = true)
    }
}

@KtorDsl
public fun Route.handleScreen(
    path: String,
    name: String? = null,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path, name) {
    pushScreen(body)
    replaceScreen(body)
    replaceAllScreen(body)
    // By default, there is reason to pop handle return a Screen
}

private fun Route.checkPluginInstalled() {
    checkNotNull(application.pluginOrNull(VoyagerNavigator)) {
        "VoyagerNavigator plugin not installed"
    }
}
