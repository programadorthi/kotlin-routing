/*
 * Copyright Koin the original author or authors.
 * https://github.com/InsertKoinIO/koin/blob/main/projects/ktor/koin-ktor/src/main/kotlin/org/koin/ktor/plugin/KoinIsolatedContextPlugin.kt
 */
package dev.programadorthi.routing.koin

import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.createApplicationPlugin
import org.koin.core.annotation.KoinInternalApi

/**
 * @author Arnaud Giuliani
 *
 * Ktor Feature class. Allows Koin Isolatd Context to start using Ktor default install(<feature>) method.
 *
 */
@OptIn(KoinInternalApi::class)
public val KoinIsolated: ApplicationPlugin<KoinConfig> =
    createApplicationPlugin(name = "Koin", createConfiguration = ::KoinConfig) {
        val koinApplication = setupKoinApplication()
        setupMonitoring(koinApplication)
        koinApplication.koin.logger.info("Koin is using Ktor isolated context")
    }
