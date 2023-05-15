package dev.programadorthi.routing.events

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal data class EventApplicationCall(
    override val application: Application,
    override val uri: String,
    override val parameters: Parameters = Parameters.Empty,
) : ApplicationCall, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = application.coroutineContext

    override val attributes: Attributes = Attributes()

    override val routeMethod: RouteMethod = EventRouteMethod

    override val name: String = ""
}
