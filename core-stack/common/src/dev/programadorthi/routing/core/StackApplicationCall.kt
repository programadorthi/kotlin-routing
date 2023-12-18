package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

public sealed class StackApplicationCall : ApplicationCall, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = application.coroutineContext

    override val attributes: Attributes = Attributes()

    public data class Pop(
        override val application: Application,
        override val name: String,
        override val uri: String,
        override val parameters: Parameters = Parameters.Empty,
    ) : StackApplicationCall() {
        override val routeMethod: RouteMethod get() = StackRouteMethod.Pop
    }

    public data class Push(
        override val application: Application,
        override val uri: String,
        override val parameters: Parameters = Parameters.Empty,
    ) : StackApplicationCall() {

        override val routeMethod: RouteMethod get() = StackRouteMethod.Push

        override val name: String get() = ""
    }

    public data class PushNamed(
        override val application: Application,
        override val name: String,
        override val parameters: Parameters = Parameters.Empty,
    ) : StackApplicationCall() {

        override val uri: String = ""

        override val routeMethod: RouteMethod get() = StackRouteMethod.Push
    }

    public data class Replace(
        override val application: Application,
        override val uri: String,
        override val parameters: Parameters = Parameters.Empty,
        val all: Boolean,
    ) : StackApplicationCall() {

        override val name: String get() = ""

        override val routeMethod: RouteMethod
            get() = if (all) StackRouteMethod.ReplaceAll else StackRouteMethod.Replace
    }

    public data class ReplaceNamed(
        override val application: Application,
        override val name: String,
        override val parameters: Parameters = Parameters.Empty,
        val all: Boolean,
    ) : StackApplicationCall() {

        override val uri: String = ""

        override val routeMethod: RouteMethod
            get() = if (all) StackRouteMethod.ReplaceAll else StackRouteMethod.Replace
    }
}
