package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.Element

internal val JavascriptRoutingAttributeKey: AttributeKey<MutableStateFlow<Element>> =
    AttributeKey("JavascriptRoutingAttributeKey")

internal var Application.routingFlow: MutableStateFlow<Element>
    get() = attributes[JavascriptRoutingAttributeKey]
    internal set(value) {
        attributes.put(JavascriptRoutingAttributeKey, value)
    }

internal var ApplicationCall.destination: Element
    get() = application.routingFlow.value
    internal set(value) {
        application.routingFlow.value = value
    }
