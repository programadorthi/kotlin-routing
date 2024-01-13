/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.core.logging

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.path
import dev.programadorthi.routing.core.application.pluginRegistry
import io.ktor.util.AttributeKey

/**
 * Generates a string representing this [ApplicationCall] suitable for logging
 */
public fun ApplicationCall.toLogString(): String = "${routeMethod.value} - ${path()}"

/**
 * Base interface for plugins that can setup MDC. See [CallLogging] plugin.
 */
public interface MDCProvider {
    /**
     * Executes [block] with [MDC] setup
     */
    public suspend fun withMDCBlock(
        call: ApplicationCall,
        block: suspend () -> Unit,
    )
}

private object EmptyMDCProvider : MDCProvider {
    override suspend fun withMDCBlock(
        call: ApplicationCall,
        block: suspend () -> Unit,
    ) = block()
}

/**
 * Returns first instance of a plugin that implements [MDCProvider]
 * or default implementation with an empty context
 */
public val Application.mdcProvider: MDCProvider
    @Suppress("UNCHECKED_CAST")
    get() =
        pluginRegistry.allKeys
            .firstNotNullOfOrNull { pluginRegistry.getOrNull(it as AttributeKey<Any>) as? MDCProvider }
            ?: EmptyMDCProvider
