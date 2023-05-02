package dev.programadorthi.routing.android

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes

private val AndroidNavigatorManagerAttributeKey: AttributeKey<AndroidNavigatorManager> =
    AttributeKey("AndroidNavigatorManager")

private var Attributes.androidNavigatorManager: AndroidNavigatorManager
    get() = get(AndroidNavigatorManagerAttributeKey)
    private set(value) {
        put(AndroidNavigatorManagerAttributeKey, value)
    }

public var Application.androidNavigatorManager: AndroidNavigatorManager
    get() = attributes.androidNavigatorManager
    set(value) {
        attributes.androidNavigatorManager = value
    }

public var ApplicationCall.androidNavigatorManager: AndroidNavigatorManager
    get() = application.androidNavigatorManager
    internal set(value) {
        application.androidNavigatorManager = value
    }
