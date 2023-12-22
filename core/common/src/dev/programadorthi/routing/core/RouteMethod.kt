package dev.programadorthi.routing.core

public interface RouteMethod {

    public val value: String

    public companion object Empty : RouteMethod {
        override val value: String = "EMPTY"
    }
}

public fun RouteMethod(value: String): RouteMethod = RouteMethodImpl(value)

private data class RouteMethodImpl(override val value: String) : RouteMethod
