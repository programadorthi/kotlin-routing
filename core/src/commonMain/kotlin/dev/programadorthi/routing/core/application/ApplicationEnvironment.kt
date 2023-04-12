/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.core.application

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
)
