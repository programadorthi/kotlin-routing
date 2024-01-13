/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.statuspages

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.createApplicationPlugin
import dev.programadorthi.routing.core.application.hooks.CallFailed
import dev.programadorthi.routing.core.logging.mdcProvider
import io.ktor.util.AttributeKey
import io.ktor.util.KtorDsl
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.reflect.instanceOf
import kotlin.reflect.KClass

private val LOGGER = KtorSimpleLogger("kotlin-routing-status-pages")

/**
 * Specifies how the exception should be handled.
 */
public typealias HandlerFunction = suspend (call: ApplicationCall, cause: Throwable) -> Unit

/**
 * A plugin that handles exceptions and status codes. Useful to configure default error pages.
 */
public val StatusPages: ApplicationPlugin<StatusPagesConfig> =
    createApplicationPlugin(
        "StatusPages",
        ::StatusPagesConfig,
    ) {
        val statusPageMarker = AttributeKey<Unit>("StatusPagesTriggered")

        val exceptions = HashMap(pluginConfig.exceptions)
        val unhandled = pluginConfig.unhandled

        fun findHandlerByValue(cause: Throwable): HandlerFunction? {
            val keys = exceptions.keys.filter { cause.instanceOf(it) }
            if (keys.isEmpty()) return null

            if (keys.size == 1) {
                return exceptions[keys.single()]
            }

            val key = selectNearestParentClass(cause, keys)
            return exceptions[key]
        }

        on(CallFailed) { call, cause ->
            if (call.attributes.contains(statusPageMarker)) return@on

            LOGGER.trace("Call ${call.uri} failed with cause $cause")

            val handler = findHandlerByValue(cause)
            if (handler == null) {
                LOGGER.trace("No handler found for exception: $cause for call ${call.uri}")
                throw cause
            }

            call.attributes.put(statusPageMarker, Unit)
            call.application.mdcProvider.withMDCBlock(call) {
                LOGGER.trace("Executing $handler for exception $cause for call ${call.uri}")
                handler(call, cause)
            }
        }

        on(BeforeFallback) { call ->
            unhandled(call)
        }
    }

/**
 * A [StatusPages] plugin configuration.
 */
@KtorDsl
public class StatusPagesConfig {
    /**
     * Provides access to exception handlers of the exception class.
     */
    public val exceptions: MutableMap<KClass<*>, HandlerFunction> = mutableMapOf()

    internal var unhandled: suspend (ApplicationCall) -> Unit = {}

    /**
     * Register an exception [handler] for the exception type [T] and its children.
     */
    public inline fun <reified T : Throwable> exception(noinline handler: suspend (call: ApplicationCall, cause: T) -> Unit): Unit =
        exception(T::class, handler)

    /**
     * Register an exception [handler] for the exception class [klass] and its children.
     */
    public fun <T : Throwable> exception(
        klass: KClass<T>,
        handler: suspend (call: ApplicationCall, T) -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST")
        val cast = handler as suspend (ApplicationCall, Throwable) -> Unit

        exceptions[klass] = cast
    }

    /**
     * Register a [handler] for the unhandled calls.
     */
    public fun unhandled(handler: suspend (ApplicationCall) -> Unit) {
        unhandled = handler
    }
}
