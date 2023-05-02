package dev.programadorthi.routing.android

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import java.lang.ref.WeakReference

interface ActivityManager {
    val currentActivity: Activity?

    val activityUnavailable: Boolean

    fun finish()

    fun start(intent: Intent)

    fun onActivityStarted(activity: Activity)
}

internal class ActivityManagerImpl(context: Context) : ActivityManager {

    // Creating an instance using Application context start with null value
    // Creating an instance using Activity or View start with top-most activity
    private var activityOnTop = WeakReference<Activity>(context.getActivity())

    override val currentActivity: Activity?
        get() = activityOnTop.get()

    override val activityUnavailable: Boolean
        get() = currentActivity?.isFinishing != false || currentActivity?.isDestroyed != false

    override fun finish() {
        currentActivity?.finish()
    }

    override fun start(intent: Intent) {
        currentActivity?.startActivity(intent)
    }

    override fun onActivityStarted(activity: Activity) {
        activityOnTop = WeakReference(activity)
    }

    private tailrec fun Context.getActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }
}