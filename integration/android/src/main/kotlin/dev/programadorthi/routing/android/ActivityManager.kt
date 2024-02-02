package dev.programadorthi.routing.android

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import io.ktor.util.pipeline.PipelineContext
import java.lang.ref.WeakReference

@PublishedApi
internal interface ActivityManager : Application.ActivityLifecycleCallbacks {
    fun currentActivity(): Activity

    fun start(
        pipelineContext: PipelineContext<Unit, ApplicationCall>,
        intent: Intent,
    )
}

internal class AndroidActivityManager : ActivityManager {
    private var currentActivity = WeakReference<Activity>(null)

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) {
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle,
    ) {
    }

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = WeakReference(activity)
    }

    override fun currentActivity(): Activity =
        currentActivity.get() ?: error(
            "Activity manager not started. Please, install AndroidActivities plugin to your route",
        )

    override fun start(
        pipelineContext: PipelineContext<Unit, ApplicationCall>,
        intent: Intent,
    ) = with(pipelineContext) {
        val activity = currentActivity()
        val options = call.activityOptions
        when (val requestCode = call.requestCode) {
            null -> activity.startActivity(intent, options)
            else -> activity.startActivityForResult(intent, requestCode, options)
        }

        when (call.routeMethod) {
            RouteMethod.Replace -> activity.finishAfterTransition()
            RouteMethod.ReplaceAll -> activity.finishAffinity()
        }
    }
}
