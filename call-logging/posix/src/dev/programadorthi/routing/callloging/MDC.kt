package dev.programadorthi.routing.callloging

import io.ktor.util.collections.ConcurrentMap

public actual object MDC {
    // TODO: Well, how to implement MDC Thread Local map in native?
    private val current = ConcurrentMap<String, String>()

    public actual fun clear() {
        current.clear()
    }

    public actual fun get(key: String): String? {
        return current[key]
    }

    public actual fun getCopyOfContextMap(): Map<String, String>? {
        return current.toMap()
    }

    public actual fun remove(key: String) {
        current.remove(key)
    }

    public actual fun setContextMap(contextMap: Map<String, String>?) {
        val other = contextMap ?: return
        current.clear()
        current.putAll(other)
    }
}
