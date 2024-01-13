package dev.programadorthi.routing.callloging

// TODO: Do I need MDC in a context like JS and others?
public expect object MDC {
    public fun clear()

    public fun getCopyOfContextMap(): Map<String, String>?

    public fun remove(key: String)

    public fun setContextMap(contextMap: Map<String, String>?)
}
