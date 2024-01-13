package dev.programadorthi.routing.voyager

import io.ktor.http.Parameters

/**
 * Add support to a previous [cafe.adriel.voyager.core.screen.Screen] listen to a pop call that
 * have [Parameters]
 *
 * @param T The result expected after the next screen was popped
 */
public interface VoyagerRoutingPopResult<in T> {
    public fun onResult(result: T)
}
