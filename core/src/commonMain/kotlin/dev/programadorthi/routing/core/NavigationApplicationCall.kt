package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.http.Parameters
import io.ktor.util.Attributes
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal data class NavigationApplicationCall(
    override val application: Application,
    override val uri: String = "",
    override val attributes: Attributes = Attributes(),
    override val parameters: Parameters = Parameters.Empty,
    public val name: String = "",
    public val pathReplacements: Parameters = Parameters.Empty,
    public val replaceAll: Boolean = false,
    public val replaceCurrent: Boolean = false,
) : ApplicationCall, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = application.coroutineContext

    init {
        check(uri.isBlank() || name.isBlank()) {
            "Both uri and name are not allowed. Provided uri: $uri and name: $name"
        }
    }
}
