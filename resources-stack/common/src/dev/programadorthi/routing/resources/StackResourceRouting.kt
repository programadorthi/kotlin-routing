package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.serializer

/**
 * Registers a typed handler [body] for a [StackRouteMethod.Push] resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.push(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    val serializer = serializer<T>()
    lateinit var builtRoute: Route
    resource<T> {
        // Calling push here to keep stack manager up-to-date
        builtRoute = push {
            // No-op
        }
        builtRoute.handle(serializer) { resource ->
            body(resource)
        }
    }
    return builtRoute
}

/**
 * Registers a typed handler [body] for a [StackRouteMethod.Replace] resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.replace(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    val serializer = serializer<T>()
    lateinit var builtRoute: Route
    resource<T> {
        // Calling replace here to keep stack manager up-to-date
        builtRoute = replace {
            // No-op
        }
        builtRoute.handle(serializer) { resource ->
            body(resource)
        }
    }
    return builtRoute
}

/**
 * Registers a typed handler [body] for a [StackRouteMethod.ReplaceAll] resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.replaceAll(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    val serializer = serializer<T>()
    lateinit var builtRoute: Route
    resource<T> {
        // Calling replaceAll here to keep stack manager up-to-date
        builtRoute = replaceAll {
            // No-op
        }
        builtRoute.handle(serializer) { resource ->
            body(resource)
        }
    }
    return builtRoute
}

/**
 * Registers a typed handler [body] for a [StackRouteMethod.Pop] resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.pop(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    val serializer = serializer<T>()
    lateinit var builtRoute: Route
    resource<T> {
        // Calling pop here to keep stack manager up-to-date
        builtRoute = pop {
            // No-op
        }
        builtRoute.handle(serializer) { resource ->
            body(resource)
        }
    }
    return builtRoute
}

public inline fun <reified T : Any> Route.handleStacked(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    push(body)
    replace(body)
    replaceAll(body)
    pop(body)
    return this
}
