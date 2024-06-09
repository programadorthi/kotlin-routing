package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.asRouting
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.voyager.history.platformPush
import dev.programadorthi.routing.voyager.history.platformReplace
import dev.programadorthi.routing.voyager.history.platformReplaceAll
import dev.programadorthi.routing.voyager.history.shouldNeglect
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
    val routing = asRouting ?: error("Your route $this must have a parent Routing")
    handle {
        screen(routing) {
            body(this)
        }
    }
}

public suspend fun PipelineContext<Unit, ApplicationCall>.screen(
    routing: Routing,
    body: suspend () -> Screen,
) {
    if (call.shouldNeglect()) {
        call.voyagerNavigator.replace(body())
        return
    }

    when (call.routeMethod) {
        RouteMethod.Push -> call.platformPush(routing, body)
        RouteMethod.Replace -> call.platformReplace(routing, body)
        RouteMethod.ReplaceAll -> call.platformReplaceAll(routing, body)
        else ->
            error(
                "Voyager needs a stack route method to work. You called a screen ${call.uri} using " +
                    "route method ${call.routeMethod} that is not supported by Voyager",
            )
    }
}
