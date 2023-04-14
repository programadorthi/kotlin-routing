package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.createApplicationPlugin
import io.ktor.util.KtorDsl

/**
 * A plugin that handles voyager navigation
 */
public val VoyagerNavigator: ApplicationPlugin<VoyagerNavigatorConfig> = createApplicationPlugin(
    "VoyagerNavigator",
    ::VoyagerNavigatorConfig,
) {
    application.voyagerEventManager = VoyagerEventManager(
        coroutineContext = application.coroutineContext,
        initialUri = pluginConfig.initialUri,
    )

    // Intercepts all call, check for a pop and emit to [VoyagerEventManager]
    on(VoyagerCallHook) { call ->
        if (call.routeMethod == RouteMethod.Pop) {
            call.voyagerEventManager.pop()
        }
    }
}

@KtorDsl
public class VoyagerNavigatorConfig {
    internal var initialUri = ""

    public fun initialUri(uri: String) {
        initialUri = uri
    }
}
