/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.statuspages

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationCallPipeline
import dev.programadorthi.routing.core.application.Hook
import io.ktor.util.pipeline.PipelinePhase
import kotlin.reflect.KClass

internal expect fun selectNearestParentClass(cause: Throwable, keys: List<KClass<*>>): KClass<*>?

internal object BeforeFallback : Hook<suspend (ApplicationCall) -> Unit> {
    override fun install(pipeline: ApplicationCallPipeline, handler: suspend (ApplicationCall) -> Unit) {
        val phase = PipelinePhase("BeforeFallback")
        pipeline.insertPhaseBefore(ApplicationCallPipeline.Fallback, phase)
        pipeline.intercept(phase) { handler(context) }
    }
}
