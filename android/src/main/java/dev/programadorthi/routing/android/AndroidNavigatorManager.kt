package dev.programadorthi.routing.android

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.StackRouteMethod
import io.ktor.http.Parameters
import kotlin.reflect.KClass
import android.app.Application as AndroidApplication

class AndroidNavigatorManager(context: Context) : ActivityLifecycleCallbacks {
    private val activityRoutes =
        mutableMapOf<KClass<*>, MutableMap<StackRouteMethod, Route>>()

    private var activityManager: ActivityManager = ActivityManagerImpl(context)
    private var registered = false
    private var starterCounter = 0

    init {
        tryAutoRegister(context)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        tryAutoRegister(p0)
    }

    override fun onActivityStarted(p0: Activity) {
        starterCounter++
        activityManager.onActivityStarted(p0)
    }

    override fun onActivityStopped(p0: Activity) {
        starterCounter--
        if (starterCounter < 1) {
            registered = false
            val androidApplication = p0.applicationContext as AndroidApplication
            androidApplication.unregisterActivityLifecycleCallbacks(this)
        }
    }

    override fun onActivityResumed(p0: Activity) = Unit
    override fun onActivityPaused(p0: Activity) = Unit
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) = Unit
    override fun onActivityDestroyed(p0: Activity) = Unit

    public fun getRouteForActivity(clazz: KClass<*>, routeMethod: StackRouteMethod): Route {
        val methodAndRoutes = activityRoutes[clazz] ?: error("No route registered to $clazz")
        return methodAndRoutes[routeMethod]
            ?: error("No route registered to $clazz with method: $routeMethod")
    }

    public fun register(clazz: KClass<*>, routeMethod: StackRouteMethod, route: Route) {
        val methodAndRoutes = activityRoutes[clazz] ?: mutableMapOf()
        val currentRoute = methodAndRoutes[routeMethod]
        check(currentRoute == null) {
            "There is already a registration to route: $route by method: $routeMethod associated to activity: $clazz"
        }
        methodAndRoutes[routeMethod] = route
        activityRoutes[clazz] = methodAndRoutes
    }

    public fun unregister(clazz: KClass<*>, routeMethod: StackRouteMethod) {
        val methodAndRoutes = activityRoutes[clazz] ?: return
        methodAndRoutes.remove(routeMethod)
        if (methodAndRoutes.isEmpty()) {
            activityRoutes.remove(clazz)
        }
    }

    public suspend fun navigate(
        clazz: Class<*>,
        parameters: Parameters,
        routeMethod: StackRouteMethod,
        body: suspend (Intent) -> Unit
    ) {
        if (activityManager.activityUnavailable) {
            return
        }

        val activity = requireNotNull(activityManager.currentActivity) {
            "There is no Activity context available to route"
        }

        val intent = Intent(activity, clazz)
        parameters.forEach { name, values ->
            if (values.size <= 1) {
                intent.putExtra(name, values.firstOrNull() ?: "")
            } else {
                intent.putExtra(name, values.toTypedArray())
            }
        }
        body(intent)

        if (activityManager.activityUnavailable) {
            return
        }

        if (routeMethod == StackRouteMethod.Pop) {
            finishCurrentActivity()
            return
        }

        if (routeMethod == StackRouteMethod.ReplaceAll) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        activityManager.start(intent)

        if (routeMethod == StackRouteMethod.Replace) {
            finishCurrentActivity()
        }
    }

    internal fun finishCurrentActivity() {
        activityManager.finish()
    }

    // Used for test only
    internal fun changeActivityManager(other: ActivityManager) {
        activityManager = other
    }

    private fun tryAutoRegister(context: Context) {
        if (registered) return

        val androidApplication = context.applicationContext as AndroidApplication
        androidApplication.registerActivityLifecycleCallbacks(this)
        registered = true
    }
}