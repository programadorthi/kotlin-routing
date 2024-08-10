/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.core.application

import dev.programadorthi.routing.core.RedirectApplicationCall
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import io.ktor.http.Parameters
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.execute
import kotlinx.coroutines.launch

private val RECEIVE_TYPE: AttributeKey<Any> = AttributeKey("KotlinRoutingReceiveType")
internal val ROUTE_INSTANCE: AttributeKey<Route> = AttributeKey("KotlinRouteInstance")

/**
 * A single act of communication between a client and server.
 */
public interface ApplicationCall {
    /**
     * An application being called.
     */
    public val application: Application

    /**
     * The application call method
     */
    public val routeMethod: RouteMethod

    /**
     * The named route to be handled.
     * The name is just an identity to the real URI that will be handled
     */
    public val name: String

    /**
     * Gets a request's URI, including a query string.
     */
    public val uri: String

    /**
     * [Attributes] attached to this call.
     */
    public val attributes: Attributes

    /**
     * Parameters associated with this call.
     */
    public val parameters: Parameters
}

/**
 * Get a request's URL path without a query string.
 */
public fun ApplicationCall.path(): String = uri.substringBefore('?')

/**
 * Get the body sent to the current [ApplicationCall]
 *
 * @return A no null value of the type requested
 * @throws IllegalStateException when the current body is null
 */
public fun <T> ApplicationCall.receive(): T =
    checkNotNull(receiveNullable<T>()) {
        "There is no body to receive in the call: $this"
    }

/**
 * Get the body sent to the current [ApplicationCall] or null instead
 *
 * @return The instance whether exists or null when missing
 */
@Suppress("UNCHECKED_CAST")
public fun <T> ApplicationCall.receiveNullable(): T? {
    val body = attributes.getOrNull(RECEIVE_TYPE)
    return body as? T
}

public fun ApplicationCall(
    application: Application,
    name: String = "",
    uri: String = "",
    routeMethod: RouteMethod = RouteMethod.Empty,
    attributes: Attributes = Attributes(),
    parameters: Parameters = Parameters.Empty,
): ApplicationCall {
    check(name.isNotBlank() || uri.isNotBlank()) {
        "No name or uri provided to create an ApplicationCall"
    }
    return ApplicationCallImpl(
        application = application,
        name = name,
        routeMethod = routeMethod,
        uri = uri,
        attributes = attributes,
        parameters = parameters,
    )
}

public fun <T : Any> ApplicationCall(
    application: Application,
    body: T,
    name: String = "",
    uri: String = "",
    routeMethod: RouteMethod = RouteMethod.Empty,
    attributes: Attributes = Attributes(),
    parameters: Parameters = Parameters.Empty,
): ApplicationCall {
    attributes.put(RECEIVE_TYPE, body)
    return ApplicationCall(
        application = application,
        name = name,
        routeMethod = routeMethod,
        uri = uri,
        attributes = attributes,
        parameters = parameters,
    )
}

public fun ApplicationCall.redirectToName(
    name: String,
    parameters: Parameters = Parameters.Empty,
    method: RouteMethod? = null,
) {
    redirect(path = "", name = name, parameters = parameters, routeMethod = method)
}

public fun ApplicationCall.redirectToPath(
    path: String,
    parameters: Parameters = Parameters.Empty,
    method: RouteMethod? = null,
) {
    redirect(path = path, name = "", parameters = parameters, routeMethod = method)
}

public fun PipelineContext<*, ApplicationCall>.route(): Route? = call.attributes.getOrNull(ROUTE_INSTANCE)

private fun ApplicationCall.redirect(
    path: String,
    name: String,
    parameters: Parameters,
    routeMethod: RouteMethod?,
) {
    with(application) {
        launch {
            execute(
                RedirectApplicationCall(
                    previousCall = this@redirect,
                    name = name,
                    uri = path,
                    coroutineContext = coroutineContext,
                    parameters = parameters,
                    newMethod = routeMethod,
                ),
            )
        }
    }
}

private data class ApplicationCallImpl(
    override val application: Application,
    override val routeMethod: RouteMethod,
    override val name: String,
    override val uri: String,
    override val attributes: Attributes,
    override val parameters: Parameters,
) : ApplicationCall
