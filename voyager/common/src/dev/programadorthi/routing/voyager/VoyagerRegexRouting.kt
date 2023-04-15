/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.route
import io.ktor.util.KtorDsl

@KtorDsl
public fun Route.pushScreen(
    path: Regex,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path) {
    pushScreen(body)
}

@KtorDsl
public fun Route.replaceScreen(
    path: Regex,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path) {
    replaceScreen(body)
}

@KtorDsl
public fun Route.replaceAllScreen(
    path: Regex,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path) {
    replaceAllScreen(body)
}

@KtorDsl
public fun Route.handleScreen(
    path: Regex,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path) {
    pushScreen(body)
    replaceScreen(body)
    replaceAllScreen(body)
    // By default, there is reason to pop handle return a Screen
}
