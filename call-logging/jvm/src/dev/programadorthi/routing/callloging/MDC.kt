package dev.programadorthi.routing.callloging

import org.slf4j.MDC as JvmMDC

public actual object MDC {
    public actual fun clear() {
        JvmMDC.clear()
    }

    public actual fun getCopyOfContextMap(): Map<String, String>? {
        return JvmMDC.getCopyOfContextMap()
    }

    public actual fun remove(key: String) {
        JvmMDC.remove(key)
    }

    public actual fun setContextMap(contextMap: Map<String, String>?) {
        JvmMDC.setContextMap(contextMap)
    }
}
