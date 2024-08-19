package dev.programadorthi.routing.callloging

public expect object MDC {
    public fun clear()

    public fun get(key: String): String?

    public fun getCopyOfContextMap(): Map<String, String>?

    public fun remove(key: String)

    public fun setContextMap(contextMap: Map<String, String>?)
}
