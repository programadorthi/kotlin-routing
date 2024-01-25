package dev.programadorthi.routing.compose.animation

import androidx.compose.animation.AnimatedVisibilityScope
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

private val ComposeRoutingAnimationScopeAttributeKey: AttributeKey<AnimatedVisibilityScope> =
    AttributeKey("ComposeRoutingAnimationScopeAttributeKey")

public var ApplicationCall.animatedVisibilityScope: AnimatedVisibilityScope?
    get() = attributes.getOrNull(ComposeRoutingAnimationScopeAttributeKey)
    internal set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingAnimationScopeAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingAnimationScopeAttributeKey)
        }
    }
