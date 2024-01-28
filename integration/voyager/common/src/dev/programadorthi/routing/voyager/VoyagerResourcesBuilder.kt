package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.resources.handle
import dev.programadorthi.routing.resources.unregisterResource
import io.ktor.util.pipeline.PipelineContext

/**
 * Registers a typed handler [body] for a resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.screen(noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Screen): Route =
    handle<T> { resource ->
        screen {
            when (resource) {
                is Screen -> resource
                else -> body(resource)
            }
        }
    }

/**
 * Registers a typed handler for a [Screen] defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 */
public inline fun <reified T : Screen> Route.screen(): Route = screen<T> { screen -> screen }

/**
 * Registers a typed handler [body] for a [RouteMethod] resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.screen(
    method: RouteMethod,
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Screen,
): Route =
    handle<T>(method = method) { resource ->
        screen {
            when (resource) {
                is Screen -> resource
                else -> body(resource)
            }
        }
    }

/**
 * Registers a typed handler for a [RouteMethod] [Screen] defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 */
public inline fun <reified T : Screen> Route.screen(method: RouteMethod): Route = screen<T>(method) { screen -> screen }

public inline fun <reified T : Screen> Routing.unregisterScreen() {
    unregisterResource<T>()
}
