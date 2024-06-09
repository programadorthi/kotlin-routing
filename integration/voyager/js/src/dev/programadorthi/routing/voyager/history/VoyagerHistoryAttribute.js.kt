package dev.programadorthi.routing.voyager.history

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

internal val VoyagerHistoryNeglectAttributeKey: AttributeKey<Boolean> =
    AttributeKey("VoyagerHistoryNeglectAttributeKey")

internal var ApplicationCall.neglect: Boolean
    get() = attributes.getOrNull(VoyagerHistoryNeglectAttributeKey) ?: false
    set(value) {
        attributes.put(VoyagerHistoryNeglectAttributeKey, value)
    }
