package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.application.Application
import io.ktor.util.AttributeKey
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.Element

internal val JavascriptRoutingAttributeKey: AttributeKey<MutableStateFlow<Element>> =
    AttributeKey("JavascriptRoutingAttributeKey")

internal var Application.routingFlow: MutableStateFlow<Element>
    get() = attributes[JavascriptRoutingAttributeKey]
    set(value) {
        attributes.put(JavascriptRoutingAttributeKey, value)
    }
