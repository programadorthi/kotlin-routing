package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.route
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.KtorDsl

@KtorDsl
public fun Route.screen(
    path: String,
    name: String? = null,
    body: suspend PipelineContext<Unit, ApplicationCall>.() -> Screen,
): Route = route(path = path, name = name) { screen(body) }

@KtorDsl
public fun Route.screen(
    path: String,
    method: RouteMethod,
    name: String? = null,
    body: suspend PipelineContext<Unit, ApplicationCall>.() -> Screen,
): Route = route(path = path, name = name, method = method) { screen(body) }

@KtorDsl
public fun Route.screen(body: suspend PipelineContext<Unit, ApplicationCall>.() -> Screen) {
    handle {
        screen {
            body(this)
        }
    }
}

public suspend fun PipelineContext<Unit, ApplicationCall>.screen(body: suspend () -> Screen) {
    val navigator = call.voyagerNavigator
    when (call.routeMethod) {
        RouteMethod.Push -> navigator.push(body())
        RouteMethod.Replace -> navigator.replace(body())
        RouteMethod.ReplaceAll -> navigator.replaceAll(body())
        else ->
            error(
                "Voyager needs a stack route method to work. You called a screen ${call.uri} using " +
                    "route method ${call.routeMethod} that is not supported by Voyager",
            )
    }
}
