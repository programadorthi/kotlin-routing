package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.StackRouting
import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.createApplicationPlugin
import dev.programadorthi.routing.core.application.install

/**
 * A plugin that handles voyager navigation
 */
public val VoyagerNavigator: ApplicationPlugin<Unit> = createApplicationPlugin(
    "VoyagerNavigator",
) {
    application.install(StackRouting) // Helps to track last pushed path

    application.voyagerNavigatorManager = VoyagerNavigatorManager(application = application)
}
