/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.OptionalParameterRouteSelector
import dev.programadorthi.routing.core.ParameterRouteSelector
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationCallPipeline
import dev.programadorthi.routing.core.application.application
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.plugin
import dev.programadorthi.routing.core.createRouteFromPath
import dev.programadorthi.routing.core.errors.BadRequestException
import dev.programadorthi.routing.core.method
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

/**
 * Registers a route [body] for a resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 */
public inline fun <reified T : Any> Route.resource(
    noinline body: Route.() -> Unit
): Route {
    val serializer = serializer<T>()
    return resource(serializer, body)
}

/**
 * Registers a typed handler [body] for a [RouteMethod.Push] resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.push(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    lateinit var builtRoute: Route
    resource<T> {
        builtRoute = method(RouteMethod.Push) {
            handle(body)
        }
    }
    return builtRoute
}

/**
 * Registers a typed handler [body] for a [RouteMethod.Replace] resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.replace(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    lateinit var builtRoute: Route
    resource<T> {
        builtRoute = method(RouteMethod.Replace) {
            handle(body)
        }
    }
    return builtRoute
}

/**
 * Registers a typed handler [body] for a [RouteMethod.ReplaceAll] resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.replaceAll(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    lateinit var builtRoute: Route
    resource<T> {
        builtRoute = method(RouteMethod.ReplaceAll) {
            handle(body)
        }
    }
    return builtRoute
}

/**
 * Registers a handler [body] for a resource defined by the [T] class.
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.handle(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    val serializer = serializer<T>()
    handle(serializer, body)
}

@PublishedApi
internal val ResourceInstanceKey: AttributeKey<Any> = AttributeKey("ResourceInstance")

/**
 * Registers a route [body] for a resource defined by the [T] class.
 *
 * @param serializer is used to decode the parameters of the request to an instance of the typed resource [T].
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 */
public fun <T : Any> Route.resource(
    serializer: KSerializer<T>,
    body: Route.() -> Unit
): Route {
    val resources = application.plugin(Resources)
    val path = resources.resourcesFormat.encodeToPathPattern(serializer)
    val queryParameters = resources.resourcesFormat.encodeToQueryParameters(serializer)
    val route = createRouteFromPath(path = path, name = null)

    return queryParameters.fold(route) { entry, query ->
        val selector = if (query.isOptional) {
            OptionalParameterRouteSelector(query.name)
        } else {
            ParameterRouteSelector(query.name)
        }
        entry.createChild(selector)
    }.apply(body)
}

/**
 * Registers a handler [body] for a resource defined by the [T] class.
 *
 * @param serializer is used to decode the parameters of the request to an instance of the typed resource [T].
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public fun <T : Any> Route.handle(
    serializer: KSerializer<T>,
    body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    intercept(ApplicationCallPipeline.Plugins) {
        val resources = application.plugin(Resources)
        try {
            val resource = resources.resourcesFormat.decodeFromParameters(serializer, call.parameters)
            call.attributes.put(ResourceInstanceKey, resource)
        } catch (cause: Throwable) {
            throw BadRequestException("Can't transform call to resource", cause)
        }
    }

    handle {
        @Suppress("UNCHECKED_CAST")
        val resource = call.attributes[ResourceInstanceKey] as T
        body(resource)
    }
}
