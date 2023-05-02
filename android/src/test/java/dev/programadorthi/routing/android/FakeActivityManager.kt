package dev.programadorthi.routing.android

import android.app.Activity
import android.content.Intent
import kotlin.reflect.KClass
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

class FakeActivityManager : ActivityManager {
    private val controllers = mutableListOf<ActivityController<*>>()
    val intents = mutableListOf<Intent>()

    var activityClassToStart: KClass<*>? = null

    override val currentActivity: Activity?
        get() = controllers.last().get()

    override val activityUnavailable: Boolean
        get() = currentActivity?.isFinishing != false || currentActivity?.isDestroyed != false

    override fun finish() {
        val controller = controllers.removeFirst()
        controller.pause().stop().destroy().close()
    }

    override fun start(intent: Intent) {
        val clazz = activityClassToStart?.java as? Class<Activity>
            ?: error("No Activity class provided to test")
        controllers += Robolectric.buildActivity(clazz, intent)
        controllers.last().setup()
        activityClassToStart = null
    }

    override fun onActivityStarted(activity: Activity) {
        intents += activity.intent
    }
}