package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.StackRouting
import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.createApplicationPlugin
import dev.programadorthi.routing.core.application.install
import io.ktor.util.KtorDsl

/**
 * A plugin that handles voyager navigation
 */
public val VoyagerNavigator: ApplicationPlugin<VoyagerNavigatorConfig> = createApplicationPlugin(
    "VoyagerNavigator",
    ::VoyagerNavigatorConfig,
) {
    application.install(StackRouting) // Helps to track last pushed path

    application.voyagerNavigatorManager = VoyagerNavigatorManager(
        application = application,
        initialUri = pluginConfig.initialUri,
    )
}

@KtorDsl
public class VoyagerNavigatorConfig {
    internal var initialUri = ""

    public fun initialUri(uri: String) {
        initialUri = uri
    }
}
