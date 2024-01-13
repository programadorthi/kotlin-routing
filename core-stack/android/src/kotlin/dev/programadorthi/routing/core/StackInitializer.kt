package dev.programadorthi.routing.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.savedstate.SavedStateRegistry
import androidx.startup.Initializer
import java.lang.ref.WeakReference

internal class StackInitializer :
    Initializer<Unit>,
    Application.ActivityLifecycleCallbacks,
    StackManagerNotifier {
    private val lock = Any()
    private var currentActivity = WeakReference<ComponentActivity?>(null)

    init {
        StackManager.stackManagerNotifier = this
    }

    //region Initializer
    override fun create(context: Context) {
        val application = context.applicationContext as Application
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
    //endregion

    //region Application.ActivityLifecycleCallbacks
    override fun onActivityCreated(
        activity: Activity,
        bundle: Bundle?,
    ) {
        updateCurrentActivity(activity = activity)
    }

    override fun onActivityStarted(activity: Activity) {
        updateCurrentActivity(activity = activity)
    }

    override fun onActivityResumed(p0: Activity) {}

    override fun onActivityPaused(p0: Activity) {}

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivitySaveInstanceState(
        p0: Activity,
        p1: Bundle,
    ) {}

    override fun onActivityDestroyed(p0: Activity) {}
    //endregion

    //region StackManagerNotifier
    override fun onRegistered(
        providerId: String,
        stackManager: StackManager,
    ) = synchronized(lock) {
        val registry = currentActivity.get()?.savedStateRegistry ?: return
        processRegistry(
            registry = registry,
            providerId = providerId,
            stackManager = stackManager,
        )
    }

    override fun onUnRegistered(providerId: String) =
        synchronized(lock) {
            val registry = currentActivity.get()?.savedStateRegistry ?: return
            registry.unregisterSavedStateProvider(providerId)
        }
    //endregion

    private fun updateCurrentActivity(activity: Activity) =
        synchronized(lock) {
            if (activity !is ComponentActivity) {
                Log.w(
                    "kotlin-routing",
                    "Your activity must be an androidx.activity.ComponentActivity. Current is: $activity",
                )
                return
            }
            currentActivity = WeakReference(activity)
            val registry = activity.savedStateRegistry
            StackManager.subscriptions().forEach { (providerId, stackManager) ->
                processRegistry(
                    registry = registry,
                    providerId = providerId,
                    stackManager = stackManager,
                )
            }
        }

    private fun processRegistry(
        registry: SavedStateRegistry,
        providerId: String,
        stackManager: StackManager,
    ) {
        val provider = StackSavedStateProvider(providerId, stackManager)
        if (registry.isRestored) {
            val previousState = registry.consumeRestoredStateForKey(providerId)
            if (previousState?.isEmpty == false) {
                provider.restoreState(previousState)
            }
        }
        registry.unregisterSavedStateProvider(providerId)
        registry.registerSavedStateProvider(key = providerId, provider = provider)
    }
}
