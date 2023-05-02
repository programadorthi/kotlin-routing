package dev.programadorthi.routing.android

import android.content.Context
import dev.programadorthi.routing.core.StackRouting
import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.createApplicationPlugin
import dev.programadorthi.routing.core.application.install
import io.ktor.util.KtorDsl

/**
 * A plugin that handles android navigation
 */
public val AndroidNavigator: ApplicationPlugin<AndroidConfig> = createApplicationPlugin(
    "AndroidNavigator",
    ::AndroidConfig,
) {
    val context = pluginConfig.context ?: error(
        """
        AndroidNavigator plugin needs a Context to work. Provide one calling: 
        install(AndroidNavigator) {
            context = YourContextHere
        }
    """.trimIndent()
    )

    application.install(StackRouting) // Helps to track last pushed path

    application.androidNavigatorManager = AndroidNavigatorManager(context = context)
}

@KtorDsl
public class AndroidConfig {
    public var context: Context? = null
}
