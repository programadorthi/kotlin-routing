/*
 * Copyright Koin the original author or authors.
 * https://github.com/InsertKoinIO/koin/blob/main/projects/ktor/koin-ktor/src/main/kotlin/org/koin/ktor/plugin/KoinPlugin.kt
 */
package dev.programadorthi.routing.koin

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.ApplicationStopping
import dev.programadorthi.routing.core.application.PluginBuilder
import dev.programadorthi.routing.core.application.createApplicationPlugin
import io.ktor.util.AttributeKey
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

private const val KOIN_KEY = "KOIN"
internal val KOIN_ATTRIBUTE_KEY = AttributeKey<KoinApplication>(KOIN_KEY)

public class KoinConfig {
    public var koinApplication: KoinApplication? = null

    public fun setup(
        koinApplication: KoinApplication? = null,
        configuration: KoinAppDeclaration,
    ) {
        val instance = koinApplication ?: KoinApplication.init()
        this.koinApplication = instance
        configuration(instance)
    }
}

/**
 * @author Arnaud Giuliani
 * @author Vinicius Carvalho
 * @author Victor Alenkov
 * @author Zak Henry
 *
 * Ktor Feature class. Allows Koin Standard Context to start using Ktor default install(<feature>) method.
 *
 */
public val Koin: ApplicationPlugin<KoinConfig> =
    createApplicationPlugin(name = "Koin", createConfiguration = ::KoinConfig) {
        val koinApplication = setupKoinApplication()
        KoinPlatformTools.defaultContext().getOrNull()?.let { stopKoin() } // for ktor auto-reload
        startKoin(koinApplication)
        setupMonitoring(koinApplication)
    }

internal fun PluginBuilder<KoinConfig>.setupKoinApplication(): KoinApplication {
    val koinApplication =
        checkNotNull(pluginConfig.koinApplication) {
            "Koin plugin not installed"
        }
    koinApplication.createEagerInstances()
    application.setKoinApplication(koinApplication)
    return koinApplication
}

public fun Application.setKoinApplication(koinApplication: KoinApplication) {
    attributes.put(KOIN_ATTRIBUTE_KEY, koinApplication)
}

internal fun PluginBuilder<KoinConfig>.setupMonitoring(koinApplication: KoinApplication) {
    val monitor = environment?.monitor
    monitor?.raise(KoinApplicationStarted, koinApplication)
    monitor?.subscribe(ApplicationStopping) {
        monitor.raise(KoinApplicationStopPreparing, koinApplication)
        koinApplication.koin.close()
        monitor.raise(KoinApplicationStopped, koinApplication)
    }
}
