package dev.programadorthi.routing.compose

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

private val ComposeRoutingPopResultAttributeKey: AttributeKey<Any> =
    AttributeKey("ComposeRoutingPopResultAttributeKey")

private val ComposeRoutingPoppedApplicationCallAttributeKey: AttributeKey<ApplicationCall> =
    AttributeKey("ComposeRoutingPoppedApplicationCallAttributeKey")

private val ComposeRoutingPoppedFlagCallAttributeKey: AttributeKey<Boolean> =
    AttributeKey("ComposeRoutingPoppedFlagCallAttributeKey")

@PublishedApi
internal var ApplicationCall.popResult: Any?
    get() = attributes.getOrNull(ComposeRoutingPopResultAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingPopResultAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingPopResultAttributeKey)
        }
    }

@PublishedApi
internal var Routing.poppedCall: ApplicationCall?
    get() = attributes.getOrNull(ComposeRoutingPoppedApplicationCallAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingPoppedApplicationCallAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingPoppedApplicationCallAttributeKey)
        }
    }

internal var Routing.popResult: Any?
    get() = attributes.getOrNull(ComposeRoutingPopResultAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingPopResultAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingPopResultAttributeKey)
        }
    }

public var ApplicationCall.popped: Boolean
    get() = attributes.getOrNull(ComposeRoutingPoppedFlagCallAttributeKey) ?: false
    internal set(value) {
        attributes.put(ComposeRoutingPoppedFlagCallAttributeKey, value)
    }

public inline fun <reified T> ApplicationCall.popResult(): T? =
    when (popResult) {
        is T -> popResult as T
        else -> null
    }

public inline fun <reified T> ApplicationCall.popResult(default: T): T = popResult() ?: default

public inline fun <reified T> ApplicationCall.popResult(default: () -> T): T = popResult() ?: default()

public fun Routing.poppedCall(): ApplicationCall? = poppedCall

public inline fun <reified T> Routing.popResult(): T? = poppedCall()?.popResult()

public inline fun <reified T> Routing.popResult(default: T): T = popResult() ?: default

public inline fun <reified T> Routing.popResult(default: () -> T): T = popResult() ?: default()
