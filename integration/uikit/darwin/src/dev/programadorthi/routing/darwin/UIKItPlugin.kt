package dev.programadorthi.routing.darwin

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.RouteScopedPlugin
import dev.programadorthi.routing.core.application.createRouteScopedPlugin
import dev.programadorthi.routing.core.application.plugin
import dev.programadorthi.routing.core.application.pluginOrNull
import dev.programadorthi.routing.core.asRouting
import io.ktor.util.AttributeKey
import io.ktor.utils.io.KtorDsl

internal val UIKitUINavigationControllerAttributeKey: AttributeKey<UIKitUINavigationController> =
    AttributeKey("UIKitUINavigationControllerAttributeKey")

public val UIKitPlugin: RouteScopedPlugin<UIKitPluginConfig> =
    createRouteScopedPlugin("UIKitPlugin", ::UIKitPluginConfig) {

        var navigationController = pluginConfig.navigationController

        if (navigationController == null) {
            val parentRouting = application.pluginOrNull(Routing)
            navigationController =
                generateSequence(seed = parentRouting) { it.parent?.asRouting }
                    .mapNotNull { it.attributes.getOrNull(UIKitUINavigationControllerAttributeKey) }
                    .firstOrNull()
        }

        val controller =
            navigationController ?: error("UIKitPlugin requires an UINavigationController")
        application.attributes.put(UIKitUINavigationControllerAttributeKey, controller)

        onCall { call ->
            call.attributes.put(UIKitUINavigationControllerAttributeKey, controller)
        }
    }

/**
 * A configuration for the [UIKitPlugin] plugin.
 */
@KtorDsl
public class UIKitPluginConfig {
    public var navigationController: UIKitUINavigationController? = null
}

@PublishedApi
internal val ApplicationCall.uiNavigationController: UIKitUINavigationController
    get() = application.uiNavigationController()

internal val Routing.uiNavigationController: UIKitUINavigationController
    get() = application.uiNavigationController()

private fun Application.uiNavigationController(): UIKitUINavigationController {
    return when (val controller = attributes.getOrNull(UIKitUINavigationControllerAttributeKey)) {
        null -> {
            plugin(UIKitPlugin)
            error("UIKit plugin not initialized. Please, install UIKitPlugin plugin")
        }

        else -> controller
    }
}
