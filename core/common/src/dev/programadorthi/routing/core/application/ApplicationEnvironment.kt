/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.core.application

import dev.programadorthi.routing.core.Routing
import io.ktor.events.Events
import io.ktor.util.logging.Logger
import kotlin.coroutines.CoroutineContext

/**
 * Represents an environment in which [Application] runs
 */
public data class ApplicationEnvironment(
    /**
     * Parent coroutine context for an application
     */
    public val parentCoroutineContext: CoroutineContext,
    /**
     * Instance of [Logger] to be used for logging.
     */
    public val log: Logger,
    /**
     * Indicates if development mode is enabled.
     */
    public val developmentMode: Boolean,
    /**
     * Application's root path (prefix, context path in servlet container).
     */
    public val rootPath: String,
    /**
     * Application's current [Routing] used to nested routing
     */
    public val parentRouting: Routing?,
    /**
     * Provides events on Application lifecycle
     */
    public val monitor: Events,
)
