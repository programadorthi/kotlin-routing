package dev.programadorthi.routing.compose

/**
 * Add support to a previous [androidx.compose.runtime.Composable] get the popped result
 */
@Suppress("UNCHECKED_CAST")
public class ComposeRoutingPopResult internal constructor(
    internal val content: Any? = null,
) {
    public fun <T> result(): T? = content as? T
}
