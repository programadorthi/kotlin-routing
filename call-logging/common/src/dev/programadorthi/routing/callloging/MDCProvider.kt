/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.callloging

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.PluginBuilder
import dev.programadorthi.routing.core.application.pluginRegistry
import dev.programadorthi.routing.core.logging.MDCProvider
import io.ktor.util.AttributeKey

internal fun PluginBuilder<CallLoggingConfig>.setupMDCProvider() {
    application.pluginRegistry.put(KtorMDCProvider.key, KtorMDCProvider(pluginConfig.mdcEntries))
}

internal class KtorMDCProvider(private val entries: List<MDCEntry>) : MDCProvider {
    override suspend fun withMDCBlock(
        call: ApplicationCall,
        block: suspend () -> Unit,
    ) {
        withMDC(entries, call, block)
    }

    companion object {
        val key = AttributeKey<KtorMDCProvider>("RoutingMDCProvider")
    }
}
