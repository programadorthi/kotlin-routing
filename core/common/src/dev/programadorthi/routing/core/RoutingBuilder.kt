/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineInterceptor

/**
 * Builds a route to match the specified [path].
 */
@KtorDsl
public fun Route.route(
    path: String,
    name: String? = null,
    build: Route.() -> Unit
): Route = createRouteFromPath(path, name)
    .apply(build)
    .registerToParents(null, build)

/**
 * Builds a route to match the specified [RouteMethod] and [path].
 */
@KtorDsl
public fun Route.route(
    path: String,
    method: RouteMethod,
    name: String? = null,
    build: Route.() -> Unit,
): Route {
    val selector = RouteMethodRouteSelector(method)
    return createRouteFromPath(path, name)
        .createChild(selector)
        .apply(build)
        .registerToParents(selector, build)
}

/**
 * Builds a route to match the specified [RouteMethod].
 */
@KtorDsl
public fun Route.method(method: RouteMethod, body: Route.() -> Unit): Route {
    val selector = RouteMethodRouteSelector(method)
    return createChild(selector)
        .apply(body)
        .registerToParents(selector, body)
}

@KtorDsl
public fun Route.handle(
    path: String,
    name: String? = null,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path, name) { handle(body) }

@KtorDsl
public fun Route.handle(
    path: String,
    method: RouteMethod,
    name: String? = null,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path, name = name, method = method) { handle(body) }

/**
 * Creates a routing entry for the specified path and name
 */
public fun Route.createRouteFromPath(path: String, name: String?): Route {
    val route = createRouteFromPath(path)
    // Registering named route
    if (!name.isNullOrBlank()) {
        val validRouting = requireNotNull(asRouting) {
            "Named route '$name' must be a Routing child."
        }
        validRouting.registerNamed(name = name, route = route)
    }
    return route
}

/**
 * Creates a routing entry for the specified path.
 */
internal fun Route.createRouteFromPath(path: String): Route {
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
    return current
}

/**
 * Look for parents [Routing] and connect them to current [Route] creating a routing
 * from top most parent until current: /topmostparent/moreparents/child
 */
private fun Route.registerToParents(
    selector: RouteMethodRouteSelector?,
    build: Route.() -> Unit
): Route {
    val parentRouting = asRouting?.parent as? Routing ?: return this
    val validPath = when (val parentPath = parentRouting.toString()) {
        "/" -> toString()
        else -> toString().substringAfter(parentPath)
    }
    if (selector == null) {
        parentRouting.route(path = validPath, name = null, build = build)
    } else {
        var path = validPath.substringBefore(selector.toString())
        if (path.endsWith('/')) {
            path = path.substring(0, path.length - 1)
        }
        parentRouting.route(
            path = path,
            method = selector.method,
            name = null,
            build = build
        )
    }
    return this
}

/**
 * A helper object for building instances of [RouteSelector] from path segments.
 */
internal object PathSegmentSelectorBuilder {
    /**
     * Builds a [RouteSelector] to match a path segment parameter with a prefix/suffix and name.
     */
    fun parseParameter(value: String): RouteSelector {
        val prefixIndex = value.indexOf('{')
        val suffixIndex = value.lastIndexOf('}')

        val prefix = if (prefixIndex == 0) null else value.substring(0, prefixIndex)
        val suffix = if (suffixIndex == value.length - 1) null else value.substring(suffixIndex + 1)

        val signature = value.substring(prefixIndex + 1, suffixIndex)
        return when {
            signature.endsWith("?") -> PathSegmentOptionalParameterRouteSelector(
                signature.dropLast(
                    1
                ),
                prefix, suffix
            )

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
    fun parseConstant(value: String): RouteSelector = when (value) {
        "*" -> PathSegmentWildcardRouteSelector
        else -> PathSegmentConstantRouteSelector(value)
    }

    /**
     * Parses a name out of segment specification.
     */
    fun parseName(value: String): String {
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
