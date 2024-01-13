package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.isStackMethod
import dev.programadorthi.routing.core.method
import dev.programadorthi.routing.core.neglect
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.resources.handle
import dev.programadorthi.routing.resources.resource
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.serializer

@KtorDsl
public fun Route.screen(
    path: String,
    name: String? = null,
    body: PipelineContext<Unit, ApplicationCall>.() -> Screen,
): Route = route(path = path, name = name) { screen(body) }

@KtorDsl
public fun Route.screen(
    path: String,
    method: RouteMethod,
    name: String? = null,
    body: PipelineContext<Unit, ApplicationCall>.() -> Screen,
): Route = route(path = path, name = name, method = method) { screen(body) }

@KtorDsl
public fun Route.screen(
    body: PipelineContext<Unit, ApplicationCall>.() -> Screen,
) {
    handle {
        screen {
            body(this)
        }
    }
}

@KtorDsl
public inline fun <reified T : Any> Route.screen(
    noinline body: PipelineContext<Unit, ApplicationCall>.(T) -> Screen
): Route = resource<T> {
    handle(serializer<T>()) { value ->
        screen {
            body(value)
        }
    }
}

public inline fun <reified T : Any> Route.screen(
    method: RouteMethod,
    noinline body: PipelineContext<Unit, ApplicationCall>.(T) -> Screen
): Route {
    lateinit var builtRoute: Route
    resource<T> {
        builtRoute = method(method) {
            handle(serializer<T>()) { value ->
                screen {
                    body(value)
                }
            }
        }
    }
    return builtRoute
}

public fun PipelineContext<Unit, ApplicationCall>.screen(
    body: () -> Screen,
) {
    if (call.neglect) {
        // Voyager is full stack based and there is no way to show a screen without put on the stack
        // So here we are calling the route to be notified only
        body()
        return
    }
    check(call.routeMethod.isStackMethod()) {
        "Voyager needs a stack route method to work. You called a screen ${call.uri} using " +
            "route method ${call.routeMethod} that is not supported by Voyager"
    }
    val navigator = call.voyagerNavigator
    when (call.routeMethod) {
        StackRouteMethod.Pop -> navigator.pop()
        StackRouteMethod.Push -> navigator.push(body())
        StackRouteMethod.Replace -> navigator.replace(body())
        StackRouteMethod.ReplaceAll -> navigator.replaceAll(body())
        // TODO: add support for popUntil
        else -> error("Never called because there is a check above")
    }
}
