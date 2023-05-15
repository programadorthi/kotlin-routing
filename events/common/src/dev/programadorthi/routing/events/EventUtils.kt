package dev.programadorthi.routing.events

import dev.programadorthi.routing.core.application.ApplicationCall

private const val SLASH = '/'

internal fun String.checkForSlash(): String {
    check(!contains(SLASH)) {
        "Event name '$this' cannot contains '$SLASH'"
    }
    return this
}

internal fun String.normalizedEvent(): String = when {
    startsWith(SLASH) -> this
    else -> "$SLASH$this"
}

public fun ApplicationCall.eventName(): String = when (routeMethod) {
    is EventRouteMethod -> uri.substring(1)
    else -> ""
}
