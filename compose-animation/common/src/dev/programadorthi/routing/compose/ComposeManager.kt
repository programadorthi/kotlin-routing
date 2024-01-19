package dev.programadorthi.routing.compose

import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.programadorthi.routing.core.Routing
import io.ktor.util.AttributeKey

private val ComposeRoutingAttributeKey: AttributeKey<SnapshotStateList<ComposeEntry>> =
    AttributeKey("ComposeRoutingAttributeKey")

private val ComposeRoutingPoppedEntryAttributeKey: AttributeKey<ComposeEntry> =
    AttributeKey("ComposeRoutingPoppedEntryAttributeKey")

internal var Routing.contentList: SnapshotStateList<ComposeEntry>
    get() = attributes[ComposeRoutingAttributeKey]
    set(value) {
        attributes.put(ComposeRoutingAttributeKey, value)
    }

internal var Routing.poppedEntry: ComposeEntry?
    get() = attributes.getOrNull(ComposeRoutingPoppedEntryAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingPoppedEntryAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingPoppedEntryAttributeKey)
        }
    }
