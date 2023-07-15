package dev.programadorthi.routing.callloging

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationCallPipeline
import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.ApplicationStarted
import dev.programadorthi.routing.core.application.ApplicationStarting
import dev.programadorthi.routing.core.application.ApplicationStopped
import dev.programadorthi.routing.core.application.ApplicationStopping
import dev.programadorthi.routing.core.application.PluginBuilder
import dev.programadorthi.routing.core.application.createApplicationPlugin
import dev.programadorthi.routing.core.application.hooks.CallSetup
import dev.programadorthi.routing.core.application.hooks.ResponseSent
import dev.programadorthi.routing.core.application.log
import io.ktor.events.Events
import io.ktor.util.AttributeKey
import io.ktor.util.date.getTimeMillis

internal val CALL_START_TIME = AttributeKey<Long>("CallStartTime")

/**
 * Returns time in millis from the moment the call was received until now
 */
public fun ApplicationCall.processingTimeMillis(clock: () -> Long = { getTimeMillis() }): Long {
    val startTime = attributes[CALL_START_TIME]
    return clock() - startTime
}

/**
 * A plugin that allows you to log incoming client requests.
 * You can configure [CallLogging] in multiple ways: specify a logging level,
 * filter requests based on a specified condition, customize log messages, and so on.
 *
 * You can learn more from [Call logging](https://ktor.io/docs/call-logging.html).
 */
public val CallLogging: ApplicationPlugin<CallLoggingConfig> = createApplicationPlugin(
    "CallLogging",
    ::CallLoggingConfig
) {
    val log = pluginConfig.logger ?: application.log
    val filters = pluginConfig.filters
    val formatCall = pluginConfig.formatCall
    val clock = pluginConfig.clock

    fun log(message: String) = when (pluginConfig.level) {
        CallLevel.ERROR -> log.error(message)
        CallLevel.WARN -> log.warn(message)
        CallLevel.INFO -> log.info(message)
        CallLevel.DEBUG -> log.debug(message)
        CallLevel.TRACE -> log.trace(message)
    }

    fun logSuccess(call: ApplicationCall) {
        if (filters.isEmpty() || filters.any { it(call) }) {
            log(formatCall(call))
        }
    }

    setupMDCProvider()
    setupLogging(application.environment.monitor, ::log)

    on(CallSetup) { call ->
        call.attributes.put(CALL_START_TIME, clock())
    }

    if (pluginConfig.mdcEntries.isEmpty()) {
        logCompletedCalls(::logSuccess)
        return@createApplicationPlugin
    }

    logCallsWithMDC(::logSuccess)
}

private fun PluginBuilder<CallLoggingConfig>.logCompletedCalls(logSuccess: (ApplicationCall) -> Unit) {
    on(ResponseSent) { call ->
        logSuccess(call)
    }
}

private fun PluginBuilder<CallLoggingConfig>.logCallsWithMDC(logSuccess: (ApplicationCall) -> Unit) {
    val entries = pluginConfig.mdcEntries

    on(MDCHook(ApplicationCallPipeline.Monitoring)) { call, block ->
        withMDC(entries, call, block)
    }

    on(MDCHook(ApplicationCallPipeline.Call)) { call, block ->
        withMDC(entries, call, block)
    }

    on(MDCHook(ApplicationCallPipeline.Fallback)) { call, block ->
        withMDC(entries, call) {
            block()
            logSuccess(call)
        }
    }
}

private fun setupLogging(events: Events, log: (String) -> Unit) {
    val starting: (Application) -> Unit = { log("Routing starting: $it") }
    val started: (Application) -> Unit = { log("Routing started: $it") }
    val stopping: (Application) -> Unit = { log("Routing stopping: $it") }
    var stopped: (Application) -> Unit = {}

    stopped = {
        log("Routing stopped: $it")
        events.unsubscribe(ApplicationStarting, starting)
        events.unsubscribe(ApplicationStarted, started)
        events.unsubscribe(ApplicationStopping, stopping)
        events.unsubscribe(ApplicationStopped, stopped)
    }

    events.subscribe(ApplicationStarting, starting)
    events.subscribe(ApplicationStarted, started)
    events.subscribe(ApplicationStopping, stopping)
    events.subscribe(ApplicationStopped, stopped)
}
