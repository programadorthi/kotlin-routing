package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

public typealias ComposeContent = @Composable (ApplicationCall) -> Unit

private val ComposeRoutingContentAttributeKey: AttributeKey<ComposeContent> =
    AttributeKey("ComposeRoutingContentAttributeKey")

public var ApplicationCall.content: ComposeContent?
    get() = attributes.getOrNull(ComposeRoutingContentAttributeKey)
    internal set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingContentAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingContentAttributeKey)
        }
    }
