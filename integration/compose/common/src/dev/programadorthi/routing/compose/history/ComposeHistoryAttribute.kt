package dev.programadorthi.routing.compose.history

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

internal val ComposeHistoryModeAttributeKey: AttributeKey<ComposeHistoryMode> =
    AttributeKey("ComposeHistoryModeAttributeKey")

internal val ComposeHistoryNeglectAttributeKey: AttributeKey<Boolean> =
    AttributeKey("ComposeHistoryNeglectAttributeKey")

internal val ComposeHistoryRestoredCallAttributeKey: AttributeKey<Boolean> =
    AttributeKey("ComposeHistoryRestoredCallAttributeKey")

internal var Application.historyMode: ComposeHistoryMode
    get() = attributes[ComposeHistoryModeAttributeKey]
    set(value) {
        attributes.put(ComposeHistoryModeAttributeKey, value)
    }

internal var Routing.historyMode: ComposeHistoryMode
    get() = application.historyMode
    set(value) {
        application.historyMode = value
    }

internal var ApplicationCall.neglect: Boolean
    get() = attributes.getOrNull(ComposeHistoryNeglectAttributeKey) ?: false
    set(value) {
        attributes.put(ComposeHistoryNeglectAttributeKey, value)
    }

internal var ApplicationCall.restored: Boolean
    get() = attributes.getOrNull(ComposeHistoryRestoredCallAttributeKey) ?: false
    set(value) {
        attributes.put(ComposeHistoryRestoredCallAttributeKey, value)
    }
