/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.statuspages

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

/**
 * Register an exception [handler] for the exception class [klass] and its children
 */
public fun <T : Throwable> StatusPagesConfig.exception(
    klass: Class<T>,
    handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
) {
    @Suppress("UNCHECKED_CAST")
    val cast = handler as suspend (ApplicationCall, Throwable) -> Unit

    exceptions[klass.kotlin] = cast
}
