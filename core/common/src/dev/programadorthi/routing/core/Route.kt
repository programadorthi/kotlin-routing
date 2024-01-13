/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationCallPipeline
import dev.programadorthi.routing.core.application.ApplicationEnvironment
import io.ktor.util.pipeline.PipelineInterceptor
import io.ktor.utils.io.KtorDsl

/**
 * Describes a node in a routing tree.
 *
 * @param parent is a parent node in the tree, or null for root node.
 * @param selector is an instance of [RouteSelector] for this node.
 */
@KtorDsl
public open class Route(
    public val parent: Route?,
    public val selector: RouteSelector,
    developmentMode: Boolean = false,
    environment: ApplicationEnvironment? = null,
) : ApplicationCallPipeline(developmentMode, environment) {
    /**
     * List of child routes for this node.
     */
    public val children: List<Route> get() = childList

    internal val childList: MutableList<Route> = mutableListOf()

    private var cachedPipeline: ApplicationCallPipeline? = null

    internal val handlers = mutableListOf<PipelineInterceptor<Unit, ApplicationCall>>()

    /**
     * Creates a child node in this node with a given [selector] or returns an existing one with the same selector.
     */
    public fun createChild(selector: RouteSelector): Route {
        val existingEntry = childList.firstOrNull { it.selector == selector }
        if (existingEntry == null) {
            val entry = Route(this, selector, developmentMode, environment)
            childList.add(entry)
            return entry
        }
        return existingEntry
    }

    /**
     * Allows using a route instance for building additional routes.
     */
    public operator fun invoke(body: Route.() -> Unit): Unit = body()

    /**
     * Installs a handler into this route which is called when the route is selected for a call.
     */
    public fun handle(handler: PipelineInterceptor<Unit, ApplicationCall>) {
        handlers.add(handler)

        // Adding a handler invalidates only pipeline for this entry
        cachedPipeline = null
    }

    override fun afterIntercepted() {
        // Adding an interceptor invalidates pipelines for all children
        // We don't need synchronisation here, because order of intercepting and acquiring pipeline is indeterminate
        // If some child already cached its pipeline, it's ok to execute with outdated pipeline
        invalidateCachesRecursively()
    }

    private fun invalidateCachesRecursively() {
        cachedPipeline = null
        childList.forEach { it.invalidateCachesRecursively() }
    }

    override fun toString(): String {
        return when (val parentRoute = parent?.toString()) {
            null ->
                when (selector) {
                    is TrailingSlashRouteSelector -> "/"
                    else -> "/$selector"
                }

            else ->
                when (selector) {
                    is TrailingSlashRouteSelector -> if (parentRoute.endsWith('/')) parentRoute else "$parentRoute/"
                    else -> if (parentRoute.endsWith('/')) "$parentRoute$selector" else "$parentRoute/$selector"
                }
        }
    }

    internal fun buildPipeline(): ApplicationCallPipeline =
        cachedPipeline ?: run {
            var current: Route? = this
            val pipeline = ApplicationCallPipeline(developmentMode, application.environment)
            val routePipelines = mutableListOf<ApplicationCallPipeline>()
            while (current != null) {
                routePipelines.add(current)
                current = current.parent
            }

            for (index in routePipelines.lastIndex downTo 0) {
                val routePipeline = routePipelines[index]
                pipeline.merge(routePipeline)
            }

            val handlers = handlers
            for (index in 0..handlers.lastIndex) {
                pipeline.intercept(Call) {
                    handlers[index].invoke(this, Unit)
                }
            }
            cachedPipeline = pipeline
            pipeline
        }
}

/**
 * Return list of endpoints with handlers under this route.
 */
public fun Route.getAllRoutes(): List<Route> {
    val endpoints = mutableListOf<Route>()
    getAllRoutes(endpoints)
    return endpoints
}

internal fun Route.allSelectors(): List<RouteSelector> {
    val selectors = mutableListOf(selector)
    var other = parent
    while (other != null && other !is Routing) {
        selectors += other.selector
        other = other.parent
    }
    // We need reverse to starting from top-most parent
    return selectors.reversed()
}

private fun Route.getAllRoutes(endpoints: MutableList<Route>) {
    if (handlers.isEmpty()) {
        endpoints.add(this)
    }
    children.forEach { it.getAllRoutes(endpoints) }
}
