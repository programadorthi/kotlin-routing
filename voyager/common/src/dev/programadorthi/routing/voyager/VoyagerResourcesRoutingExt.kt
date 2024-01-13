package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.pluginOrNull

public inline fun <reified T : Any> Routing.push(resource: T) {
    checkNotNull(application.pluginOrNull(VoyagerResources)) {
        "VoyagerResources plugin not installed"
    }
    push(path = application.href(resource))
}

public inline fun <reified T : Any> Routing.replace(resource: T) {
    checkNotNull(application.pluginOrNull(VoyagerResources)) {
        "VoyagerResources plugin not installed"
    }
    replace(path = application.href(resource))
}

public inline fun <reified T : Any> Routing.replaceAll(resource: T) {
    checkNotNull(application.pluginOrNull(VoyagerResources)) {
        "VoyagerResources plugin not installed"
    }
    replaceAll(path = application.href(resource))
}
