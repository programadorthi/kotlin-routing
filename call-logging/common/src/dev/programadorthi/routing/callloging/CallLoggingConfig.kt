/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.callloging

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.KtorDsl
import io.ktor.util.date.getTimeMillis
import io.ktor.util.logging.Logger

/**
 * A configuration for the [CallLogging] plugin.
 */
@KtorDsl
public class CallLoggingConfig {
    internal var clock: () -> Long = { getTimeMillis() }
    internal val filters = mutableListOf<(ApplicationCall) -> Boolean>()
    internal val mdcEntries = mutableListOf<MDCEntry>()
    internal var formatCall: (ApplicationCall) -> String = ::defaultFormat

    /**
     * Specifies a logging level for the [CallLogging] plugin.
     * The default level is [CallLevel.INFO].
     */
    public var level: CallLevel = CallLevel.INFO

    /**
     * Specifies a [Logger] used to log requests.
     * By default, uses [ApplicationEnvironment.log].
     */
    public var logger: Logger? = null

    /**
     * Allows you to add conditions for filtering requests.
     * In the example below, only requests made to `/api/v1` get into a log:
     * ```kotlin
     * filter { call ->
     *     call.request.path().startsWith("/api/v1")
     * }
     * ```
     *
     * @see [CallLogging]
     */
    public fun filter(predicate: (ApplicationCall) -> Boolean) {
        filters.add(predicate)
    }

    /**
     * Puts a diagnostic context value to [MDC] with the specified [name] and computed using the [provider] function.
     * A value is available in MDC only during [ApplicationCall] lifetime and is removed after a call processing.
     *
     * @see [CallLogging]
     */
    public fun mdc(name: String, provider: (ApplicationCall) -> String?) {
        mdcEntries.add(MDCEntry(name, provider))
    }

    /**
     * Allows you to configure a call log message.
     *
     * @see [CallLogging]
     */
    public fun format(formatter: (ApplicationCall) -> String) {
        formatCall = formatter
    }

    /**
     * Allows you to configure a clock that will be used to measure call processing time.
     *
     * @see [CallLogging]
     */
    public fun clock(clock: () -> Long) {
        this.clock = clock
    }

    private fun defaultFormat(call: ApplicationCall): String =
        """${call.routeMethod} -> name: ${call.name}, uri: ${call.uri}"""
}
