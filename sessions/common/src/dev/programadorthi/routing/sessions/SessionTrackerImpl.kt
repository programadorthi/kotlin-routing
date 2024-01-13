package dev.programadorthi.routing.sessions

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.log
import io.ktor.util.AttributeKey
import kotlin.reflect.KClass

public class SessionTrackerImpl<S : Any>(
    private val type: KClass<S>,
    private val storage: SessionStorage,
    private val serializer: SessionSerializer<S>?,
) : SessionTracker<S> {
    private val sessionIdKey: AttributeKey<String> = AttributeKey("$type")

    @Suppress("UNCHECKED_CAST")
    override suspend fun load(
        call: ApplicationCall,
        transport: String?,
    ): S? {
        require(storage is SessionStorageMemory || serializer != null) {
            "To persist a session you must use InMemory SessionStorage or provide a custom SessionSerializer"
        }

        val sessionId = transport ?: return null
        call.attributes.put(sessionIdKey, sessionId)

        return runCatching {
            if (storage is SessionStorageMemory || serializer == null) {
                storage.read(sessionId) as? S
            } else {
                val content = storage.read(sessionId) as String
                serializer.deserialize(content)
            }
        }.getOrElse { ex ->
            call.application.log.debug("Failed to deserialize session: $sessionId", ex)

            // Remove the wrong session identifier if no related session was found
            call.attributes.remove(sessionIdKey)

            null
        }
    }

    override suspend fun clear(call: ApplicationCall) {
        val sessionId = call.attributes.takeOrNull(sessionIdKey)
        if (sessionId != null) {
            storage.invalidate(sessionId)
        }
    }

    override fun validate(value: S) {
        if (!type.isInstance(value)) {
            throw IllegalArgumentException("Value for this session tracker expected to be of type $type but was $value")
        }
    }

    override suspend fun store(
        call: ApplicationCall,
        value: S,
    ): String {
        require(storage is SessionStorageMemory || serializer != null) {
            "To persist a session you must use InMemory SessionStorage or provide a custom SessionSerializer"
        }

        val sessionId = call.attributes.computeIfAbsent(sessionIdKey) { "$type" }
        if (storage is SessionStorageMemory || serializer == null) {
            storage.write(sessionId, value)
        } else {
            val serialized = serializer.serialize(value)
            storage.write(sessionId, serialized)
        }
        return sessionId
    }
}
