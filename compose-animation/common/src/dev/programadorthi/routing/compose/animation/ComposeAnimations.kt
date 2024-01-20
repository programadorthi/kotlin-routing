package dev.programadorthi.routing.compose.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey
import kotlin.jvm.JvmSuppressWildcards

internal typealias Animation<T> = @JvmSuppressWildcards
AnimatedContentTransitionScope<ApplicationCall>.() -> T

private val ComposeRoutingEnterTransitionAttributeKey: AttributeKey<Animation<EnterTransition>> =
    AttributeKey("ComposeRoutingEnterTransitionAttributeKey")

private val ComposeRoutingExitTransitionAttributeKey: AttributeKey<Animation<ExitTransition>> =
    AttributeKey("ComposeRoutingExitTransitionAttributeKey")

private val ComposeRoutingPopEnterTransitionAttributeKey: AttributeKey<Animation<EnterTransition>> =
    AttributeKey("ComposeRoutingPopEnterTransitionAttributeKey")

private val ComposeRoutingPopExitTransitionAttributeKey: AttributeKey<Animation<ExitTransition>> =
    AttributeKey("ComposeRoutingPopExitTransitionAttributeKey")

internal var ApplicationCall.enterTransition: Animation<EnterTransition>?
    get() = attributes.getOrNull(ComposeRoutingEnterTransitionAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingEnterTransitionAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingEnterTransitionAttributeKey)
        }
    }

internal var ApplicationCall.exitTransition: Animation<ExitTransition>?
    get() = attributes.getOrNull(ComposeRoutingExitTransitionAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingExitTransitionAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingExitTransitionAttributeKey)
        }
    }

internal var ApplicationCall.popEnterTransition: Animation<EnterTransition>?
    get() = attributes.getOrNull(ComposeRoutingPopEnterTransitionAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingPopEnterTransitionAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingPopEnterTransitionAttributeKey)
        }
    }

internal var ApplicationCall.popExitTransition: Animation<ExitTransition>?
    get() = attributes.getOrNull(ComposeRoutingPopExitTransitionAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingPopExitTransitionAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingPopExitTransitionAttributeKey)
        }
    }
