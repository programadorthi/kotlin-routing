package dev.programadorthi.routing.compose

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

private val ComposeRoutingResourceAttributeKey: AttributeKey<Any> =
    AttributeKey("ComposeRoutingResourceAttributeKey")

@PublishedApi
internal var ApplicationCall.resource: Any?
    get() = attributes.getOrNull(ComposeRoutingResourceAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingResourceAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingResourceAttributeKey)
        }
    }

public inline fun <reified T> ApplicationCall.resource(): T? =
    when (resource) {
        is T -> resource as T
        else -> null
    }
