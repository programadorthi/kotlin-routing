package dev.programadorthi.routing.android.fake

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dev.programadorthi.routing.android.ActivityManager
import dev.programadorthi.routing.android.AndroidActivityManager
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

internal class FakeActivityManager<T : Activity>(
    mainActivity: Class<T>,
) : ActivityManager {
    private val manager = AndroidActivityManager()

    init {
        @Suppress("UNCHECKED_CAST")
        val controller = setup(mainActivity as Class<Activity>)
        manager.onActivityStarted(controller.get())
    }

    override fun currentActivity(): Activity = manager.currentActivity()

    @Suppress("UNCHECKED_CAST")
    override fun start(
        pipelineContext: PipelineContext<Unit, ApplicationCall>,
        intent: Intent,
    ) {
        manager.start(pipelineContext, intent)

        val name = intent.component?.className ?: error("No class name provided on the Intent")
        val activityClass = Class.forName(name) as Class<Activity>
        setup(activityClass) // Will trigger onActivityStarted
    }

    override fun onActivityStarted(activity: Activity) {
        manager.onActivityStarted(activity)
    }

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle,
    ) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {}

    private fun setup(activityClass: Class<Activity>): ActivityController<Activity> =
        Robolectric.buildActivity(activityClass).setup() as ActivityController<Activity>
}
