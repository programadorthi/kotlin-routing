/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.callloging

import dev.programadorthi.routing.core.application.ApplicationCall
import kotlinx.coroutines.withContext

internal class MDCEntry(val name: String, val provider: (ApplicationCall) -> String?)

internal suspend inline fun withMDC(
    mdcEntries: List<MDCEntry>,
    call: ApplicationCall,
    crossinline block: suspend () -> Unit,
) {
    withContext(MDCContext(mdcEntries.setup(call))) {
        try {
            block()
        } finally {
            mdcEntries.cleanup()
        }
    }
}

internal fun List<MDCEntry>.setup(call: ApplicationCall): Map<String, String> {
    val result = HashMap<String, String>()

    forEach { entry ->
        val provider = runCatching { entry.provider(call) }.getOrNull()
        provider?.let { mdcValue ->
            result[entry.name] = mdcValue
        }
    }

    return result
}

internal fun List<MDCEntry>.cleanup() {
    forEach {
        MDC.remove(it.name)
    }
}
