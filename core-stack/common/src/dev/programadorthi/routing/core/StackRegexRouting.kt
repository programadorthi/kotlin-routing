package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineInterceptor

@KtorDsl
public fun Route.pop(
    path: Regex,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path) { pop(body) }

@KtorDsl
public fun Route.push(
    path: Regex,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path) { push(body) }

@KtorDsl
public fun Route.replace(
    path: Regex,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path) { replace(body) }

@KtorDsl
public fun Route.replaceAll(
    path: Regex,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path) { replaceAll(body) }
