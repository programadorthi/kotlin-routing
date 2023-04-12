package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.http.Parameters
import io.ktor.util.Attributes
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

public sealed class NavigationApplicationCall : ApplicationCall, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = application.coroutineContext

    override val attributes: Attributes = Attributes()

    public data class Push(
        override val application: Application,
        override val uri: String,
        override val parameters: Parameters = Parameters.Empty,
    ) : NavigationApplicationCall()

    public data class PushNamed(
        override val application: Application,
        override val parameters: Parameters = Parameters.Empty,
        public val name: String,
        public val pathReplacements: Parameters = Parameters.Empty,
    ) : NavigationApplicationCall() {

        override val uri: String = ""
    }

    public data class Replace(
        override val application: Application,
        override val uri: String,
        override val parameters: Parameters = Parameters.Empty,
        public val all: Boolean = false,
    ) : NavigationApplicationCall()

    public data class ReplaceNamed(
        override val application: Application,
        override val parameters: Parameters = Parameters.Empty,
        public val name: String,
        public val pathReplacements: Parameters = Parameters.Empty,
        public val all: Boolean = false,
    ) : NavigationApplicationCall() {

        override val uri: String = ""
    }
}
