/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.callloging

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationCallPipeline
import dev.programadorthi.routing.core.application.Hook
import dev.programadorthi.routing.core.application.call
import io.ktor.util.pipeline.PipelinePhase

@Suppress("FunctionName")
internal fun MDCHook(phase: PipelinePhase) =
    object : Hook<suspend (ApplicationCall, suspend () -> Unit) -> Unit> {
        override fun install(
            pipeline: ApplicationCallPipeline,
            handler: suspend (ApplicationCall, suspend () -> Unit) -> Unit,
        ) {
            val mdcPhase = PipelinePhase("${phase.name}MDC")
            pipeline.insertPhaseBefore(phase, mdcPhase)

            pipeline.intercept(mdcPhase) {
                handler(call, ::proceed)
            }
        }
    }
