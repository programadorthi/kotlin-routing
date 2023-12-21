package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.pluginOrNull
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll

public inline fun <reified T : Any> Routing.push(resource: T, neglect: Boolean = false) {
    checkNotNull(application.pluginOrNull(Resources)) {
        "Resources plugin not installed"
    }
    push(
        path = application.href(resource),
        neglect = neglect,
    )
}

public inline fun <reified T : Any> Routing.replace(resource: T, neglect: Boolean = false) {
    checkNotNull(application.pluginOrNull(Resources)) {
        "Resources plugin not installed"
    }
    replace(
        path = application.href(resource),
        neglect = neglect,
    )
}

public inline fun <reified T : Any> Routing.replaceAll(resource: T, neglect: Boolean = false) {
    checkNotNull(application.pluginOrNull(Resources)) {
        "Resources plugin not installed"
    }
    replaceAll(
        path = application.href(resource),
        neglect = neglect,
    )
}
