package dev.programadorthi.routing.callloging

public actual object MDC {
    private val current = mutableMapOf<String, String>()

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
        current.run {
            clear()
            putAll(other)
        }
    }
}
