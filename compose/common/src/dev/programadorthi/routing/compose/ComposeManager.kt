package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.programadorthi.routing.core.Routing
import io.ktor.util.AttributeKey

public typealias Content = @Composable () -> Unit

private val ComposeRoutingAttributeKey: AttributeKey<SnapshotStateList<Content>> =
    AttributeKey("ComposeRoutingAttributeKey")

private val ComposeRoutingPopResultAttributeKey: AttributeKey<Any> =
    AttributeKey("ComposeRoutingPopResultAttributeKey")

internal var Routing.contentList: SnapshotStateList<Content>
    get() = attributes[ComposeRoutingAttributeKey]
    set(value) {
        attributes.put(ComposeRoutingAttributeKey, value)
    }

internal var Routing.popResult: Any?
    get() = attributes.getOrNull(ComposeRoutingPopResultAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(ComposeRoutingPopResultAttributeKey, value)
        } else {
            attributes.remove(ComposeRoutingPopResultAttributeKey)
        }
    }
