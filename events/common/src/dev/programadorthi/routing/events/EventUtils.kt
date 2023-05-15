package dev.programadorthi.routing.events

private const val SLASH = '/'

public fun String.checkForSlash(): String {
    check(!contains(SLASH)) {
        "Event name '$this' cannot contains '$SLASH'"
    }
    return this
}
