package dev.programadorthi.routing.compose

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import dev.programadorthi.routing.core.application.ApplicationCall
import kotlin.jvm.JvmSuppressWildcards

internal typealias Animation<T> = @JvmSuppressWildcards
AnimatedContentTransitionScope<ComposeEntry>.() -> T

public data class ComposeEntry(
    val call: ApplicationCall,
    internal val content: @Composable AnimatedContentScope.() -> Unit,
) {
    @PublishedApi
    internal var popResult: Any? = null

    @PublishedApi
    internal var resource: Any? = null

    internal var enterTransition: Animation<EnterTransition>? = null
    internal var exitTransition: Animation<ExitTransition>? = null
    internal var popEnterTransition: Animation<EnterTransition>? = enterTransition
    internal var popExitTransition: Animation<ExitTransition>? = exitTransition

    public var popped: Boolean = false
        internal set

    public inline fun <reified T> popResult(): T? =
        when (popResult) {
            is T -> popResult as T
            else -> null
        }

    public inline fun <reified T> popResult(default: T): T = popResult() ?: default

    public inline fun <reified T> popResult(default: () -> T): T = popResult() ?: default()

    public inline fun <reified T> resource(): T? =
        when (resource) {
            is T -> resource as T
            else -> null
        }
}
