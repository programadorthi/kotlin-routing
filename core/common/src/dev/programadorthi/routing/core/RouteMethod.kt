package dev.programadorthi.routing.core

public interface RouteMethod {
    public val value: String

    public companion object {
        public val Empty: RouteMethod = RouteMethod("EMPTY")
    }
}

public fun RouteMethod(value: String): RouteMethod = RouteMethodImpl(value)

private data class RouteMethodImpl(override val value: String) : RouteMethod
