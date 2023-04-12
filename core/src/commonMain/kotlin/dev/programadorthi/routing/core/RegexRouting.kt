/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineInterceptor

/**
 * Builds a route to match the specified regex [path].
 * Named parameters from regex can be accessed via [ApplicationCall.parameters].
 *
 * Example:
 * ```
 * route(Regex("/(?<number>\\d+)")) {
 *     get("/hello") {
 *         val number = call.parameters["number"]
 *         ...
 *     }
 * }
 * ```
 */
@KtorDsl
public fun Route.route(path: Regex, build: Route.() -> Unit): Route = createRouteFromRegexPath(path).apply(build)

@KtorDsl
public fun Route.handle(
    path: Regex,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path) { handle(body) }

@KtorDsl
public fun Route.push(
    path: Regex,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path) { push(body) }

@KtorDsl
public fun Route.replace(
    path: Regex,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path) { replace(body) }

private fun Route.createRouteFromRegexPath(regex: Regex): Route {
    return this.createChild(PathSegmentRegexRouteSelector(regex))
}

public class PathSegmentRegexRouteSelector(private val regex: Regex) : RouteSelector() {

    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        val prefix = if (regex.pattern.startsWith('/')) "/" else ""
        val postfix = if (regex.pattern.endsWith('/')) "/" else ""
        val pathSegments = context.segments.drop(segmentIndex).joinToString("/", prefix, postfix)
        val result = regex.find(pathSegments) ?: return RouteSelectorEvaluation.Failed

        val segmentIncrement = result.value.length.let { consumedLength ->
            if (pathSegments.length == consumedLength) {
                context.segments.size - segmentIndex
            } else if (pathSegments[consumedLength] == '/') {
                val segments = result.value.substring(0, consumedLength)
                val count = segments.count { it == '/' }
                if (prefix == "/") count else count + 1
            } else {
                return RouteSelectorEvaluation.Failed
            }
        }

        val groups = result.groups as MatchNamedGroupCollection
        val parameters = Parameters.build {
            GROUP_NAME_MATCHER.findAll(regex.pattern).forEach { matchResult ->
                val (name) = matchResult.destructured
                val value = groups[name]?.value ?: ""
                append(name, value)
            }
        }
        return RouteSelectorEvaluation.Success(
            quality = RouteSelectorEvaluation.qualityQueryParameter,
            parameters = parameters,
            segmentIncrement = segmentIncrement
        )
    }

    override fun toString(): String = "Regex(${regex.pattern})"

    public companion object {
        private val GROUP_NAME_MATCHER = Regex("\\(\\?<(\\p{Alpha}\\p{Alnum}*)>[^)]*\\)")
    }
}
