package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.navigator.Navigator
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes

private val VoyagerNavigatorAttributeKey: AttributeKey<Navigator> =
    AttributeKey("VoyagerNavigator")

private var Attributes.voyagerNavigator: Navigator
    get() = get(VoyagerNavigatorAttributeKey)
    private set(value) {
        put(VoyagerNavigatorAttributeKey, value)
    }

internal var Application.voyagerNavigator: Navigator
    get() = attributes.voyagerNavigator
    set(value) {
        attributes.voyagerNavigator = value
    }

internal var ApplicationCall.voyagerNavigator: Navigator
    get() = application.voyagerNavigator
    set(value) {
        application.voyagerNavigator = value
    }
