/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import io.ktor.http.Parameters
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineInterceptor
import io.ktor.util.pipeline.execute
import kotlinx.coroutines.launch

/**
 * Builds a route to match the specified [path].
 */
@KtorDsl
public fun Route.route(
    path: String,
    name: String? = null,
    build: Route.() -> Unit
): Route = createRouteFromPath(path, name).apply(build)

@KtorDsl
public fun Route.handle(
    path: String,
    name: String? = null,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path, name) { handle(body) }

@KtorDsl
public fun Route.push(
    path: String,
    name: String? = null,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path, name) { push(body) }

@KtorDsl
public fun Route.replace(
    path: String,
    name: String? = null,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path, name) { replace(body) }

@KtorDsl
public fun Route.pop(
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    handleNavigation(
        body = body,
        predicate = { it is NavigationApplicationCall.Pop }
    )
    return this
}

@KtorDsl
public fun Route.push(
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    handleNavigation(
        body = body,
        predicate = { it is NavigationApplicationCall.Push }
    )
    return this
}

@KtorDsl
public fun Route.replace(
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    handleNavigation(
        body = body,
        predicate = { it is NavigationApplicationCall.Replace }
    )
    return this
}

@KtorDsl
public fun Route.redirectToName(name: String, pathParameters: Parameters = Parameters.Empty) {
    check(this !is Routing) {
        "Redirect root is not allowed. You can do this changing rootPath on initialization"
    }
    redirect(path = "", name = name, pathParameters = pathParameters)
}

@KtorDsl
public fun Route.redirectToPath(path: String) {
    check(this !is Routing) {
        "Redirect root is not allowed. You can do this changing rootPath on initialization"
    }
    redirect(path = path, name = "")
}

internal fun Route.handleNavigation(
    predicate: (NavigationApplicationCall) -> Boolean,
    body: PipelineInterceptor<Unit, ApplicationCall>,
) {
    handle {
        val routingCall = call as RoutingApplicationCall
        val navigationCall = routingCall.previousCall as NavigationApplicationCall
        if (predicate(navigationCall)) {
            body(this@handle, Unit)
        }
    }
}

/**
 * Creates a routing entry for the specified path.
 */
public fun Route.createRouteFromPath(path: String, name: String?): Route {
    val parts = RoutingPath.parse(path).parts
    var current: Route = this
    for (index in parts.indices) {
        val (value, kind) = parts[index]
        val selector = when (kind) {
            RoutingPathSegmentKind.Parameter -> PathSegmentSelectorBuilder.parseParameter(value)
            RoutingPathSegmentKind.Constant -> PathSegmentSelectorBuilder.parseConstant(value)
        }
        // there may already be entry with same selector, so join them
        current = current.createChild(selector)
    }
    if (path.endsWith("/")) {
        current = current.createChild(TrailingSlashRouteSelector)
    }
    // Registering named route
    if (!name.isNullOrBlank()) {
        routing().registerNamed(name = name, route = current)
    }
    return current
}

/**
 * A helper object for building instances of [RouteSelector] from path segments.
 */
internal object PathSegmentSelectorBuilder {
    /**
     * Builds a [RouteSelector] to match a path segment parameter with a prefix/suffix and name.
     */
    public fun parseParameter(value: String): RouteSelector {
        val prefixIndex = value.indexOf('{')
        val suffixIndex = value.lastIndexOf('}')

        val prefix = if (prefixIndex == 0) null else value.substring(0, prefixIndex)
        val suffix = if (suffixIndex == value.length - 1) null else value.substring(suffixIndex + 1)

        val signature = value.substring(prefixIndex + 1, suffixIndex)
        return when {
            signature.endsWith("?") -> PathSegmentOptionalParameterRouteSelector(signature.dropLast(1), prefix, suffix)
            signature.endsWith("...") -> {
                if (!suffix.isNullOrEmpty()) {
                    throw IllegalArgumentException("Suffix after tailcard is not supported")
                }
                PathSegmentTailcardRouteSelector(signature.dropLast(3), prefix ?: "")
            }

            else -> PathSegmentParameterRouteSelector(signature, prefix, suffix)
        }
    }

    /**
     * Builds a [RouteSelector] to match a constant or wildcard segment parameter.
     */
    public fun parseConstant(value: String): RouteSelector = when (value) {
        "*" -> PathSegmentWildcardRouteSelector
        else -> PathSegmentConstantRouteSelector(value)
    }

    /**
     * Parses a name out of segment specification.
     */
    public fun parseName(value: String): String {
        val prefix = value.substringBefore('{', "")
        val suffix = value.substringAfterLast('}', "")
        val signature = value.substring(prefix.length + 1, value.length - suffix.length - 1)
        return when {
            signature.endsWith("?") -> signature.dropLast(1)
            signature.endsWith("...") -> signature.dropLast(3)
            else -> signature
        }
    }
}

private fun Route.redirect(path: String, name: String, pathParameters: Parameters = Parameters.Empty) {
    check(this !is Routing) {
        "Redirect root is not allowed. You can do this changing rootPath on initialization"
    }
    handle {
        val called = call
        with(called.application) {
            launch {
                execute(
                    RedirectApplicationCall(
                        previousCall = called,
                        name = name,
                        uri = path,
                        coroutineContext = this@with.coroutineContext,
                        pathParameters = pathParameters,
                    )
                )
            }
        }
    }
}
