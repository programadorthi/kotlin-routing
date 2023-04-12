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
    ) : NavigationApplicationCall() {

        override val routeMethod: RouteMethod = RouteMethod.Pop
    }

    public data class Push(
        override val application: Application,
        override val uri: String,
        override val parameters: Parameters = Parameters.Empty,
    ) : NavigationApplicationCall() {

        override val routeMethod: RouteMethod = RouteMethod.Push
    }

    public data class PushNamed(
        override val application: Application,
        override val parameters: Parameters = Parameters.Empty,
        public val name: String,
        public val pathParameters: Parameters = Parameters.Empty,
    ) : NavigationApplicationCall() {

        override val uri: String = ""

        override val routeMethod: RouteMethod = RouteMethod.Push
    }

    public data class Replace(
        override val application: Application,
        override val uri: String,
        override val parameters: Parameters = Parameters.Empty,
        public val all: Boolean = false,
    ) : NavigationApplicationCall() {

        override val routeMethod: RouteMethod = RouteMethod.Replace
    }

    public data class ReplaceNamed(
        override val application: Application,
        override val parameters: Parameters = Parameters.Empty,
        public val name: String,
        public val pathParameters: Parameters = Parameters.Empty,
        public val all: Boolean = false,
    ) : NavigationApplicationCall() {

        override val uri: String = ""

        override val routeMethod: RouteMethod = RouteMethod.Replace
    }
}
