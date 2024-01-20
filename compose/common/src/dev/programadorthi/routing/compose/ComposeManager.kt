package dev.programadorthi.routing.compose

import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

private val ComposeRoutingAttributeKey: AttributeKey<SnapshotStateList<ApplicationCall>> =
    AttributeKey("ComposeRoutingAttributeKey")

internal var Routing.callStack: SnapshotStateList<ApplicationCall>
    get() = attributes[ComposeRoutingAttributeKey]
    set(value) {
        attributes.put(ComposeRoutingAttributeKey, value)
    }
