package dev.programadorthi.routing.android.activity

import android.app.Activity
import android.app.Application
import android.content.Intent
import dev.programadorthi.routing.android.AndroidActivities
import dev.programadorthi.routing.android.activity
import dev.programadorthi.routing.android.activity.activity.Activity1
import dev.programadorthi.routing.android.activity.activity.Activity2
import dev.programadorthi.routing.android.activity.activity.Activity3
import dev.programadorthi.routing.android.activity.activity.Activity4
import dev.programadorthi.routing.android.currentActivity
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing

class MainApplication : Application() {

    val router = routing {
        install(AndroidActivities) {
            context = this@MainApplication
        }

        activity(path = Activity1.PATH) {
            Intent(call.currentActivity, Activity1::class.java)
        }

        activity(path = Activity2.PATH) {
            Intent(call.currentActivity, Activity2::class.java)
        }

        activity(path = Activity3.PATH) {
            Intent(call.currentActivity, Activity3::class.java)
        }

        activity(path = Activity4.PATH) {
            Intent(call.currentActivity, Activity4::class.java)
        }
    }

}

val Activity.router: Routing
    get() = (application as MainApplication).router