package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.Node

internal val JavascriptRoutingAttributeKey: AttributeKey<MutableStateFlow<Node?>> =
    AttributeKey("JavascriptRoutingAttributeKey")

internal var Application.routingFlow: MutableStateFlow<Node?>
    get() = attributes[JavascriptRoutingAttributeKey]
    internal set(value) {
        attributes.put(JavascriptRoutingAttributeKey, value)
    }

internal var ApplicationCall.destination: Node?
    get() = application.routingFlow.value
    internal set(value) {
        application.routingFlow.value = value
    }
