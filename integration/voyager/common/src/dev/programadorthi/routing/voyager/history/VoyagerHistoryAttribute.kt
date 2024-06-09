package dev.programadorthi.routing.voyager.history

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.Application
import io.ktor.util.AttributeKey

internal val VoyagerHistoryModeAttributeKey: AttributeKey<VoyagerHistoryMode> =
    AttributeKey("VoyagerHistoryModeAttributeKey")

internal var Application.historyMode: VoyagerHistoryMode
    get() = attributes[VoyagerHistoryModeAttributeKey]
    set(value) {
        attributes.put(VoyagerHistoryModeAttributeKey, value)
    }

internal var Routing.historyMode: VoyagerHistoryMode
    get() = application.historyMode
    set(value) {
        application.historyMode = value
    }
