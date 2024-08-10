package dev.programadorthi.routing.kodein

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.route
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import org.kodein.di.DI
import org.kodein.di.LazyDI

// attribute key for storing injector in a call
public val KodeinDIKey: AttributeKey<DI> = AttributeKey<DI>("KodeinDI")

/**
 * Getting the global [DI] container from the [Application]
 */
public fun Application.closestDI(): LazyDI = LazyDI { attributes[KodeinDIKey] }

/**
 * Getting the global [DI] container from the [Application] parameter
 */
public fun closestDI(getApplication: () -> Application): LazyDI = getApplication().closestDI()

/**
 * Getting the global [DI] container from the [ApplicationCall]
 */
public fun ApplicationCall.closestDI(): LazyDI = closestDI { application }

/**
 * Getting the global [DI] container from the [Routing] feature
 */
public fun Routing.closestDI(): LazyDI = closestDI { application }

/**
 * Getting the global or local (if extended) [DI] container from the current [Route]
 * by browsering all the routing tree until we get to the root level, the [Routing] feature
 *
 * @throws IllegalStateException if there is no [DI] container
 */
public fun Route.closestDI(): LazyDI {
    val attrs =
        when {
            this is Routing -> application.attributes
            else -> attributes
        }
    // Is there an inner DI container for this Route ?
    val routeDI = attrs.getOrNull(KodeinDIKey)
    if (routeDI is LazyDI) {
        return routeDI
    }
    return parent?.closestDI() ?: error("No DI container found for [$this]")
}

/**
 * Getting the global [DI] container from the [ApplicationCall]
 */
public fun PipelineContext<*, ApplicationCall>.closestDI(): LazyDI {
    val route =
        requireNotNull(route()) {
            "Invalid context to get the closestDI: $call"
        }
    return route.closestDI()
}
