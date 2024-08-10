package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ROUTE_INSTANCE
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal class RoutingApplicationCall(
    override val coroutineContext: CoroutineContext,
    override val routeMethod: RouteMethod,
    val previousCall: ApplicationCall,
    val route: Route,
    parameters: Parameters,
) : ApplicationCall, CoroutineScope {
    override val application: Application get() = previousCall.application
    override val name: String get() = previousCall.name
    override val uri: String get() = previousCall.uri

    override val parameters: Parameters by lazy(LazyThreadSafetyMode.NONE) {
        Parameters.build {
            appendAll(previousCall.parameters)
            appendMissing(parameters)
        }
    }

    override val attributes: Attributes by lazy(LazyThreadSafetyMode.NONE) {
        previousCall.attributes.apply {
            put(ROUTE_INSTANCE, route)
        }
    }

    override fun toString(): String = "RoutingApplicationCall(route=$route)"
}
