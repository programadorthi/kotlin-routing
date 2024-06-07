package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

internal typealias ComposeContent = @Composable (ApplicationCall) -> Unit

private val ComposeRoutingContentAttributeKey: AttributeKey<ComposeContent> =
    AttributeKey("ComposeRoutingContentAttributeKey")

internal var ApplicationCall.content: ComposeContent?
    get() = attributes.getOrNull(ComposeRoutingContentAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingContentAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingContentAttributeKey)
        }
    }
