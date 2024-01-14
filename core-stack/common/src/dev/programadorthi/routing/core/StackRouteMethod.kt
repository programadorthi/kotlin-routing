package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall

public fun RouteMethod.isStackMethod(): Boolean =
    isStackPop() ||
        this == RouteMethod.Push ||
        this == RouteMethod.Replace ||
        this == RouteMethod.ReplaceAll

public fun RouteMethod.isStackPop(): Boolean = this == RouteMethod.Pop

public fun ApplicationCall.isPop(): Boolean = routeMethod.isStackPop()
