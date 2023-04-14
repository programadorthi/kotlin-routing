package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationCallPipeline
import dev.programadorthi.routing.core.application.Hook
import dev.programadorthi.routing.core.application.call
import io.ktor.util.pipeline.PipelinePhase

/**
 * A hook that is invoked after any routing call
 */
internal object VoyagerCallHook : Hook<suspend (ApplicationCall) -> Unit> {
    override fun install(pipeline: ApplicationCallPipeline, handler: suspend (ApplicationCall) -> Unit) {
        val phase = PipelinePhase("VoyagerCall")
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Call, phase)
        pipeline.intercept(phase) {
            handler(call)
        }
    }
}
