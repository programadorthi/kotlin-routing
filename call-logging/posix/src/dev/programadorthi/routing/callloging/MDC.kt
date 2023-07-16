package dev.programadorthi.routing.callloging

public actual object MDC {
    // TODO: Well, how to implement MDC in native?

    public actual fun clear() {
        // no-op for now
    }

    public actual fun getCopyOfContextMap(): Map<String, String>? {
        return null
    }

    public actual fun remove(key: String) {
        // no-op for now
    }

    public actual fun setContextMap(contextMap: Map<String, String>?) {
        // no-op for now
    }
}
