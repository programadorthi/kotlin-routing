/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.sessions

import kotlin.reflect.KClass

public inline fun <reified S : Any> SessionsConfig.session() {
    session<S> {}
}

/**
 * Configures [Sessions] to pass the serialized session's data.
 * The [block] parameter allows you to configure additional settings, for example, sign and encrypt session data.
 */
public inline fun <reified S : Any> SessionsConfig.session(block: HeaderSessionBuilder<S>.() -> Unit) {
    val sessionType = S::class
    val builder = HeaderSessionBuilder(sessionType).apply(block)
    session(sessionType, null, builder)
}

/**
 * Configures [Sessions] to pass a session identifier in a [name] HTTP header and
 * store the serialized session's data in the server [storage].
 * The [block] parameter allows you to configure additional settings, for example, sign and encrypt session data.
 */
public inline fun <reified S : Any> SessionsConfig.session(
    storage: SessionStorage,
    block: HeaderSessionBuilder<S>.() -> Unit,
) {
    val sessionType = S::class
    val builder = HeaderSessionBuilder(sessionType).apply(block)
    session(sessionType, storage, builder)
}

@PublishedApi
internal fun <S : Any> SessionsConfig.session(
    sessionType: KClass<S>,
    storage: SessionStorage?,
    builder: HeaderSessionBuilder<S>,
) {
    val transport = SessionTransportImpl("$sessionType", builder.transformers)
    val tracker = SessionTrackerImpl(sessionType, storage ?: SessionStorageMemory(), builder.serializer)
    val provider = SessionProvider("$sessionType", sessionType, transport, tracker)
    register(provider)
}

/**
 * A configuration that allows you to configure header settings for [Sessions].
 * @property type session instance type
 */
public open class HeaderSessionBuilder<S : Any>
    @PublishedApi
    internal constructor(
        private val type: KClass<S>,
    ) {
        /**
         * Specifies a serializer used to serialize session data.
         */
        public var serializer: SessionSerializer<S>? = null

        private val _transformers = mutableListOf<SessionTransportTransformer>()

        /**
         * Gets transformers used to sign and encrypt session data.
         */
        public val transformers: List<SessionTransportTransformer> get() = _transformers

        /**
         * Registers a [transformer] used to sign and encrypt session data.
         */
        public fun transform(transformer: SessionTransportTransformer) {
            _transformers.add(transformer)
        }
    }
