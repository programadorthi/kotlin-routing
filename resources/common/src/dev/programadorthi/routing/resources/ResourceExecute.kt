package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.pluginOrNull
import dev.programadorthi.routing.core.call
import io.ktor.util.pipeline.execute
import kotlinx.coroutines.launch

public inline fun <reified T : Any> Routing.execute(
    resource: T,
    routeMethod: RouteMethod = RouteMethod.Empty,
) {
    checkNotNull(application.pluginOrNull(Resources)) {
        "Resources plugin not installed"
    }
    call(uri = application.href(resource), routeMethod = routeMethod)
}

public inline fun <reified T : Any> ApplicationCall.redirectTo(resource: T) {
    checkNotNull(application.pluginOrNull(Resources)) {
        "Resources plugin not installed"
    }
    with(application) {
        launch {
            execute(
                ApplicationCall(
                    application = application,
                    uri = href(resource),
                    routeMethod = routeMethod,
                ),
            )
        }
    }
}

public inline fun <reified T : Any> Routing.push(resource: T) {
    execute(resource = resource, routeMethod = RouteMethod.Push)
}

public inline fun <reified T : Any> Routing.replace(resource: T) {
    execute(resource = resource, routeMethod = RouteMethod.Replace)
}

public inline fun <reified T : Any> Routing.replaceAll(resource: T) {
    execute(resource = resource, routeMethod = RouteMethod.ReplaceAll)
}
