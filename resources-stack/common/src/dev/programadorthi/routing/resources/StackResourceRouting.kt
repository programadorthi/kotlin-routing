package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

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
    return handle(method = StackRouteMethod.Push, body = body)
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
    return handle(method = StackRouteMethod.Replace, body = body)
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
    return handle(method = StackRouteMethod.ReplaceAll, body = body)
}
