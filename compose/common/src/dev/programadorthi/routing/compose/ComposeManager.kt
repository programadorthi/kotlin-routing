package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

public typealias Content = @Composable () -> Unit

private val ComposeRoutingAttributeKey: AttributeKey<MutableState<Content>> =
    AttributeKey("ComposeRoutingAttributeKey")

internal var ApplicationCall.content: Content
    get() = application.contentState.value
    set(value) {
        application.contentState.value = value
    }

internal var Application.contentState: MutableState<Content>
    get() = attributes[ComposeRoutingAttributeKey]
    set(value) {
        attributes.put(ComposeRoutingAttributeKey, value)
    }
