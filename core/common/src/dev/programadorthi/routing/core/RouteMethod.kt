package dev.programadorthi.routing.core

public interface RouteMethod {

    public val value: String

    public companion object Empty : RouteMethod {
        override val value: String = "EMPTY"
    }
}
