package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal class ResolveApplicationCall(
    override val coroutineContext: CoroutineContext,
    override val uri: String = "",
    private val previousCall: ApplicationCall,
    parameters: Parameters,
) : ApplicationCall, CoroutineScope {

    override val application: Application get() = previousCall.application

    override val attributes: Attributes get() = previousCall.attributes

    override val routeMethod: RouteMethod get() = previousCall.routeMethod

    override val name: String get() = previousCall.name

    override val parameters: Parameters by lazy(LazyThreadSafetyMode.NONE) {
        Parameters.build {
            appendAll(previousCall.parameters)
            appendMissing(parameters)
        }
    }

    override fun toString(): String = "NamedApplicationCall(name=$name, uri=$uri)"
}
