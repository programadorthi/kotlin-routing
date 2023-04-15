/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.pluginOrNull
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineInterceptor

@KtorDsl
public fun Route.push(
    path: String,
    name: String? = null,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path, name = name) { push(body) }

@KtorDsl
public fun Route.push(
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    checkPluginInstalled()
    return method(StackRouteMethod.Push) { handle(body) }
}

@KtorDsl
public fun Route.replace(
    path: String,
    name: String? = null,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path, name = name) { replace(body) }

@KtorDsl
public fun Route.replace(
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    checkPluginInstalled()
    return method(StackRouteMethod.Replace) { handle(body) }
}

@KtorDsl
public fun Route.replaceAll(
    path: String,
    name: String? = null,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path, name = name) { replaceAll(body) }

@KtorDsl
public fun Route.replaceAll(
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    checkPluginInstalled()
    return method(StackRouteMethod.ReplaceAll) { handle(body) }
}

@KtorDsl
public fun Route.pop(
    path: String,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path) { pop(body) }

@KtorDsl
public fun Route.pop(
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    checkPluginInstalled()
    return method(StackRouteMethod.Pop) { handle(body) }
}

private fun Route.checkPluginInstalled() {
    checkNotNull(application.pluginOrNull(StackRouting)) {
        "StackRouting plugin not installed"
    }
}
