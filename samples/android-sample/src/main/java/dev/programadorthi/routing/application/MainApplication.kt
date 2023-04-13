package dev.programadorthi.routing.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.core.routing

val Context.currentActivity: Activity?
    get() = (applicationContext as MainApplication).activityHandler.currentActivity

val Context.router: Routing
    get() = (applicationContext as MainApplication).router

class MainApplication : Application() {
    val activityHandler = ActivityHandler()

    // You can create a router anywhere
    // I am creating here to routing to main activity wherever I can
    val router = routing {
        route(path = "/main", name = "main") {
            push {
                startMain()
            }
            replace {
                currentActivity?.finish()
                startMain(replaceAll = false)
            }
            replaceAll {
                startMain(replaceAll = true)
            }
            pop {
                currentActivity?.finish()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(activityHandler)
    }

    private fun startMain(replaceAll: Boolean = false) {
        // Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag.
        // To avoid the message above, I am using the last activity to start other
        val ctx: Context = currentActivity ?: this
        val intent = Intent(this, MainActivity::class.java)
        if (replaceAll) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        ctx.startActivity(intent)
    }
}