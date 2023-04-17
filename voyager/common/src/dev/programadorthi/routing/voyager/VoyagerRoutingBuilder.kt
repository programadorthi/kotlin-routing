package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.pluginOrNull
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll
import dev.programadorthi.routing.core.route
import io.ktor.util.KtorDsl

@KtorDsl
public fun Route.screen(
    path: String,
    name: String? = null,
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path, name) { screen(body) }

@KtorDsl
public fun Route.screen(
    body: VoyagerPipelineInterceptor<Unit, ApplicationCall>,
): Route {
    checkPluginInstalled()
    push {
        val screen = body(this, Unit)
        call.voyagerNavigatorManager.push(screen)
    }

    replace {
        val screen = body(this, Unit)
        call.voyagerNavigatorManager.replace(screen = screen, replaceAll = false)
    }

    replaceAll {
        val screen = body(this, Unit)
        call.voyagerNavigatorManager.replace(screen = screen, replaceAll = true)
    }

    // On pop handle there is no need to call body and create a Screen that will never be used
    pop {
        call.voyagerNavigatorManager.pop()
    }

    return this
}

private fun Route.checkPluginInstalled() {
    checkNotNull(application.pluginOrNull(VoyagerNavigator)) {
        "VoyagerNavigator plugin not installed"
    }
}
