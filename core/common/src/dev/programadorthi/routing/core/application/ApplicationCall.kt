/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.core.application

import dev.programadorthi.routing.core.RedirectApplicationCall
import dev.programadorthi.routing.core.RouteMethod
import io.ktor.http.Parameters
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import io.ktor.util.pipeline.execute
import io.ktor.util.reflect.TypeInfo
import kotlinx.coroutines.launch

private val RECEIVE_TYPE_KEY: AttributeKey<TypeInfo> = AttributeKey("ReceiveType")

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
 * The [TypeInfo] recorded from the last [call.receive<Type>()] call.
 */
public var ApplicationCall.receiveType: TypeInfo
    get() = attributes[RECEIVE_TYPE_KEY]
    internal set(value) {
        attributes.put(RECEIVE_TYPE_KEY, value)
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

public fun ApplicationCall.redirectToName(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    redirect(path = "", name = name, parameters = parameters)
}

public fun ApplicationCall.redirectToPath(
    path: String,
    parameters: Parameters = Parameters.Empty,
) {
    redirect(path = path, name = "", parameters = parameters)
}

private fun ApplicationCall.redirect(
    path: String,
    name: String,
    parameters: Parameters,
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
