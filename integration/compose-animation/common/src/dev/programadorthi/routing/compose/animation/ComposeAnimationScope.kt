package dev.programadorthi.routing.compose.animation

import androidx.compose.animation.AnimatedVisibilityScope
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

private val ComposeRoutingAnimationScopeAttributeKey: AttributeKey<AnimatedVisibilityScope> =
    AttributeKey("ComposeRoutingAnimationScopeAttributeKey")

internal var ApplicationCall.animatedVisibilityScope: AnimatedVisibilityScope
    get() = attributes[ComposeRoutingAnimationScopeAttributeKey]
    internal set(value) {
        attributes.put(ComposeRoutingAnimationScopeAttributeKey, value)
    }
