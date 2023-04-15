package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationCallPipeline
import dev.programadorthi.routing.core.application.Hook
import io.ktor.util.pipeline.PipelinePhase

internal object StackAfterCall : Hook<suspend (ApplicationCall) -> Unit> {
    override fun install(pipeline: ApplicationCallPipeline, handler: suspend (ApplicationCall) -> Unit) {
        val phase = PipelinePhase("StackAfterCall")
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Call, phase)
        pipeline.intercept(phase) { handler(context) }
    }
}

internal object StackBeforeCall : Hook<suspend (ApplicationCall) -> Unit> {
    override fun install(pipeline: ApplicationCallPipeline, handler: suspend (ApplicationCall) -> Unit) {
        val phase = PipelinePhase("StackBeforeCall")
        pipeline.insertPhaseBefore(ApplicationCallPipeline.Call, phase)
        pipeline.intercept(phase) { handler(context) }
    }
}
