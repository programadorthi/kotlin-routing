package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal data class RedirectApplicationCall(
    override val uri: String = "",
    override val coroutineContext: CoroutineContext,
    val previousCall: ApplicationCall,
    val name: String = "",
    val pathParameters: Parameters = Parameters.Empty,
) : ApplicationCall, CoroutineScope {

    override val application: Application get() = previousCall.application

    override val attributes: Attributes get() = previousCall.attributes

    override val parameters: Parameters get() = previousCall.parameters

    // Redirect is always a replace
    override val routeMethod: dev.programadorthi.routing.core.RouteMethod get() = dev.programadorthi.routing.core.RouteMethod.Companion.Replace

    override fun toString(): String = "RedirectApplicationCall(from=${previousCall.uri}, toName=$name, toPath=$uri)"
}
