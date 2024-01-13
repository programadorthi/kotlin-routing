package dev.programadorthi.routing.sessions

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

internal class SessionTransportImpl(
    key: String,
    private val transformers: List<SessionTransportTransformer>,
) : SessionTransport {
    private val transportId: AttributeKey<String> = AttributeKey(key)

    override fun receive(call: ApplicationCall): String? {
        return transformers.transformRead(call.attributes.getOrNull(transportId))
    }

    override fun send(
        call: ApplicationCall,
        value: String,
    ) {
        call.attributes.put(transportId, transformers.transformWrite(value))
    }

    override fun clear(call: ApplicationCall) {
        call.attributes.remove(transportId)
    }
}
