package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.Element

internal val JavascriptNeglectAttributeKey: AttributeKey<Boolean> =
    AttributeKey("JavascriptNeglectAttributeKey")

internal val JavascriptRoutingAttributeKey: AttributeKey<MutableStateFlow<Element>> =
    AttributeKey("JavascriptRoutingAttributeKey")

internal var ApplicationCall.neglect: Boolean
    get() = attributes.getOrNull(JavascriptNeglectAttributeKey) ?: false
    set(value) {
        attributes.put(JavascriptNeglectAttributeKey, value)
    }

internal var Application.routingFlow: MutableStateFlow<Element>
    get() = attributes[JavascriptRoutingAttributeKey]
    set(value) {
        attributes.put(JavascriptRoutingAttributeKey, value)
    }
