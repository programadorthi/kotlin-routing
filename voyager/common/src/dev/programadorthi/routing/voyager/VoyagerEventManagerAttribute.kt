package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes

private val VoyagerEventManagerAttributeKey: AttributeKey<VoyagerEventManager> =
    AttributeKey("VoyagerEventManager")

private var Attributes.voyagerEventManager: VoyagerEventManager
    get() = get(VoyagerEventManagerAttributeKey)
    private set(value) {
        put(VoyagerEventManagerAttributeKey, value)
    }

internal var Application.voyagerEventManager: VoyagerEventManager
    get() = attributes.voyagerEventManager
    set(value) {
        attributes.voyagerEventManager = value
    }

internal var ApplicationCall.voyagerEventManager: VoyagerEventManager
    get() = application.voyagerEventManager
    set(value) {
        application.voyagerEventManager = value
    }
