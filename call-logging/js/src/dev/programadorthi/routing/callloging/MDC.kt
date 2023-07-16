package dev.programadorthi.routing.callloging

public actual object MDC {
    // TODO: Is javascript multi thread? I don't know
    private val current = mutableMapOf<String, String>()

    public actual fun clear() {
        current.clear()
    }

    public actual fun getCopyOfContextMap(): Map<String, String>? {
        return current
    }

    public actual fun remove(key: String) {
        current.remove(key)
    }

    public actual fun setContextMap(contextMap: Map<String, String>?) {
        val other = contextMap ?: return
        current.run {
            clear()
            putAll(other)
        }
    }
}
