package dev.programadorthi.routing.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

class ActivityHandler : Application.ActivityLifecycleCallbacks {
    private var lastActivity = WeakReference<Activity?>(null)

    val currentActivity: Activity?
        get() = lastActivity.get()

    override fun onActivityCreated(p0: Activity, p1: Bundle?) = Unit

    override fun onActivityStarted(p0: Activity) {
        lastActivity = WeakReference(p0)
    }

    override fun onActivityResumed(p0: Activity) = Unit

    override fun onActivityPaused(p0: Activity) = Unit

    override fun onActivityStopped(p0: Activity) = Unit

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) = Unit

    override fun onActivityDestroyed(p0: Activity) = Unit
}