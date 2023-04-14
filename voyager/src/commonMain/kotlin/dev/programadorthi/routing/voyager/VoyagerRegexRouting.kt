/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.route
import io.ktor.util.KtorDsl

@KtorDsl
public fun Route.push(
    path: Regex,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path) {
    push(body)
}

@KtorDsl
public fun Route.replace(
    path: Regex,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path) {
    replace(body)
}

@KtorDsl
public fun Route.replaceAll(
    path: Regex,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path) {
    replaceAll(body)
}

@KtorDsl
public fun Route.handle(
    path: Regex,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path) {
    push(body)
    replace(body)
    replaceAll(body)
}
