package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.RouteMethod

public object VoyagerRouteMethod {
    public val Push: RouteMethod = RouteMethod("PUSH")
    public val Replace: RouteMethod = RouteMethod("REPLACE")
    public val ReplaceAll: RouteMethod = RouteMethod("REPLACE_ALL")

    public fun parse(method: String): RouteMethod {
        return when (method) {
            Push.value -> Push
            Replace.value -> Replace
            ReplaceAll.value -> ReplaceAll
            else -> RouteMethod(method)
        }
    }
}
