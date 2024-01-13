/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.core.application.hooks

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationCallPipeline
import dev.programadorthi.routing.core.application.Hook
import dev.programadorthi.routing.core.application.call
import io.ktor.events.EventDefinition
import io.ktor.util.pipeline.PipelinePhase
import kotlinx.coroutines.coroutineScope

/**
 * A hook that is invoked as a first step in processing a call.
 * Useful for validating, updating a call based on proxy information, etc.
 */
public object CallSetup : Hook<suspend (ApplicationCall) -> Unit> {
    override fun install(
        pipeline: ApplicationCallPipeline,
        handler: suspend (ApplicationCall) -> Unit,
    ) {
        pipeline.intercept(ApplicationCallPipeline.Setup) {
            handler(call)
        }
    }
}

/**
 * A hook that is invoked when a call fails with an exception.
 */
public object CallFailed : Hook<suspend (call: ApplicationCall, cause: Throwable) -> Unit> {
    private val phase = PipelinePhase("BeforeSetup")

    override fun install(
        pipeline: ApplicationCallPipeline,
        handler: suspend (call: ApplicationCall, cause: Throwable) -> Unit,
    ) {
        pipeline.insertPhaseBefore(ApplicationCallPipeline.Setup, phase)
        pipeline.intercept(phase) {
            try {
                coroutineScope {
                    proceed()
                }
            } catch (cause: Throwable) {
                handler(call, cause)
            }
        }
    }
}

/**
 * A hook that is invoked when response was successfully sent to a client.
 * Useful for cleaning up opened resources or finishing measurements.
 */
public object ResponseSent : Hook<suspend (ApplicationCall) -> Unit> {
    private val phase = PipelinePhase("AfterCall")

    override fun install(
        pipeline: ApplicationCallPipeline,
        handler: suspend (ApplicationCall) -> Unit,
    ) {
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Call, phase)
        pipeline.intercept(phase) {
            proceed()
            handler(call)
        }
    }
}

/**
 * A shortcut hook for [ApplicationEnvironment.monitor] subscription.
 */
public class MonitoringEvent<Param : Any, Event : EventDefinition<Param>>(
    private val event: Event,
) : Hook<(Param) -> Unit> {
    override fun install(
        pipeline: ApplicationCallPipeline,
        handler: (Param) -> Unit,
    ) {
        pipeline.environment!!.monitor.subscribe(event) {
            handler(it)
        }
    }
}
