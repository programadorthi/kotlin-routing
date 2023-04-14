package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

public sealed class NavigationApplicationCall : ApplicationCall, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = application.coroutineContext

    override val attributes: Attributes = Attributes()

    public data class Pop(
        override val application: Application,
        override val uri: String,
        override val parameters: Parameters = Parameters.Empty,
    ) : dev.programadorthi.routing.core.NavigationApplicationCall() {

        override val routeMethod: dev.programadorthi.routing.core.RouteMethod =
            dev.programadorthi.routing.core.RouteMethod.Companion.Pop
    }

    public data class Push(
        override val application: Application,
        override val uri: String,
        override val parameters: Parameters = Parameters.Empty,
    ) : dev.programadorthi.routing.core.NavigationApplicationCall() {

        override val routeMethod: dev.programadorthi.routing.core.RouteMethod =
            dev.programadorthi.routing.core.RouteMethod.Companion.Push
    }

    public data class PushNamed(
        override val application: Application,
        override val parameters: Parameters = Parameters.Empty,
        public val name: String,
        public val pathParameters: Parameters = Parameters.Empty,
    ) : dev.programadorthi.routing.core.NavigationApplicationCall() {

        override val uri: String = ""

        override val routeMethod: dev.programadorthi.routing.core.RouteMethod =
            dev.programadorthi.routing.core.RouteMethod.Companion.Push
    }

    public data class Replace(
        override val application: Application,
        override val uri: String,
        override val routeMethod: dev.programadorthi.routing.core.RouteMethod,
        override val parameters: Parameters = Parameters.Empty,
    ) : dev.programadorthi.routing.core.NavigationApplicationCall()

    public data class ReplaceNamed(
        override val application: Application,
        override val routeMethod: dev.programadorthi.routing.core.RouteMethod,
        override val parameters: Parameters = Parameters.Empty,
        public val name: String,
        public val pathParameters: Parameters = Parameters.Empty,
        public val all: Boolean = false,
    ) : dev.programadorthi.routing.core.NavigationApplicationCall() {

        override val uri: String = ""
    }
}
