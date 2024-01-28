package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

public typealias ComposeContent = @Composable (ApplicationCall) -> Unit

private val ComposeRoutingContentAttributeKey: AttributeKey<ComposeContent> =
    AttributeKey("ComposeRoutingContentAttributeKey")

public var ApplicationCall.content: ComposeContent
    get() = attributes[ComposeRoutingContentAttributeKey]
    internal set(value) {
        attributes.put(ComposeRoutingContentAttributeKey, value)
    }
