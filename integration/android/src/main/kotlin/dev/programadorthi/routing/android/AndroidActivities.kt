package dev.programadorthi.routing.android

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import android.app.Application as AndroidApplication

internal val AndroidActivityManagerKey =
    AttributeKey<ActivityManager>("AndroidActivityManagerKey")

public val AndroidActivities: RouteScopedPlugin<AndroidActivitiesConfig> =
    createRouteScopedPlugin("AndroidActivities", ::AndroidActivitiesConfig) {

        val context = pluginConfig.context
        val parentRouting = application.pluginOrNull(Routing)

        var manager =
            generateSequence(seed = parentRouting) { it.parent?.asRouting }
                .mapNotNull { it.attributes.getOrNull(AndroidActivityManagerKey) }
                .firstOrNull()

        if (manager == null) {
            val applicationContext =
                when (context) {
                    is AndroidApplication -> context
                    else -> context.applicationContext
                }
            val application = applicationContext as AndroidApplication
            manager = pluginConfig.manager
            application.registerActivityLifecycleCallbacks(manager)
        }

        val activity =
            generateSequence(seed = context) { (it as? ContextWrapper)?.baseContext }
                .mapNotNull { it as? Activity }
                .firstOrNull()

        if (activity != null) {
            manager.onActivityStarted(activity)
        }

        application.attributes.put(AndroidActivityManagerKey, manager)

        onCall { call ->
            call.attributes.put(AndroidActivityManagerKey, manager)
        }
    }

/**
 * A configuration for the [AndroidActivities] plugin.
 */
@KtorDsl
public class AndroidActivitiesConfig {
    public lateinit var context: Context

    internal var manager: ActivityManager = AndroidActivityManager()
}

@PublishedApi
internal val ApplicationCall.activityManager: ActivityManager
    get() = application.activityManager()

internal val Routing.activityManager: ActivityManager
    get() = application.activityManager()

private fun Application.activityManager(): ActivityManager {
    return when (val manager = attributes.getOrNull(AndroidActivityManagerKey)) {
        null -> {
            plugin(AndroidActivities)
            error("There is no started activity manager. Please, install AndroidActivities plugin")
        }

        else -> manager
    }
}
