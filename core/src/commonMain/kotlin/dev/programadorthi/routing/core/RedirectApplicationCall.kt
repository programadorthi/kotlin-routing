package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.http.Parameters
import io.ktor.util.Attributes
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal data class RedirectApplicationCall(
    public val previousCall: ApplicationCall,
    public val name: String = "",
    public val attempt: Int = 1,
    override val uri: String = "",
    override val coroutineContext: CoroutineContext,
) : ApplicationCall, CoroutineScope {

    override val application: Application get() = previousCall.application
    override val attributes: Attributes get() = previousCall.attributes

    override val parameters: Parameters get() = previousCall.parameters

    override fun toString(): String = "RedirectApplicationCall(from=${previousCall.uri}, toName=$name, toPath=$uri)"
}
