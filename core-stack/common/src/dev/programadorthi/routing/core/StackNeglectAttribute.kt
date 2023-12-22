package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

internal val StackNeglectAttributeKey: AttributeKey<Unit> = AttributeKey("StackNeglectAttributeKey")

public var ApplicationCall.neglect: Boolean
    get() = attributes.contains(StackNeglectAttributeKey)
    internal set(value) = if (value) {
        attributes.put(StackNeglectAttributeKey, Unit)
    } else {
        attributes.remove(StackNeglectAttributeKey)
    }
