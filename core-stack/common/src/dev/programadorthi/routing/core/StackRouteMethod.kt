package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall

public object StackRouteMethod {
    public val Pop: RouteMethod = RouteMethod("POP")
    public val Push: RouteMethod = RouteMethod("PUSH")
    public val Replace: RouteMethod = RouteMethod("REPLACE")
    public val ReplaceAll: RouteMethod = RouteMethod("REPLACE_ALL")

    public fun parse(method: String): RouteMethod {
        return when (method) {
            Pop.value -> Pop
            Push.value -> Push
            Replace.value -> Replace
            ReplaceAll.value -> ReplaceAll
            else -> RouteMethod(method)
        }
    }
}

public fun RouteMethod.isStackMethod(): Boolean =
    isStackPop() ||
        this == StackRouteMethod.Push ||
        this == StackRouteMethod.Replace ||
        this == StackRouteMethod.ReplaceAll

public fun RouteMethod.isStackPop(): Boolean = this == StackRouteMethod.Pop

public fun ApplicationCall.isPop(): Boolean = routeMethod.isStackPop()
