package dev.programadorthi.routing.android

import android.app.Activity
import android.content.Intent
import dev.programadorthi.routing.core.Routing

public fun Routing.popActivity(result: Intent? = null) {
    val activity = activityManager.currentActivity()

    if (result != null) {
        activity.setResult(Activity.RESULT_OK, result)
    } else {
        activity.setResult(Activity.RESULT_CANCELED)
    }

    activity.finishAfterTransition()
}
