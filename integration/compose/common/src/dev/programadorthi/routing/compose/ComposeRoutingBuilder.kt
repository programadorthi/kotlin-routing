package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import dev.programadorthi.routing.compose.history.platformPush
import dev.programadorthi.routing.compose.history.platformReplace
import dev.programadorthi.routing.compose.history.platformReplaceAll
import dev.programadorthi.routing.compose.history.restored
import dev.programadorthi.routing.compose.history.shouldNeglect
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.asRouting
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.resources.handle
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.KtorDsl

@KtorDsl
public fun Route.composable(
    path: String,
    name: String? = null,
    body: @Composable PipelineContext<Unit, ApplicationCall>.() -> Unit,
): Route = route(path = path, name = name) { composable(body) }

@KtorDsl
public fun Route.composable(
    path: String,
    method: RouteMethod,
    name: String? = null,
    body: @Composable PipelineContext<Unit, ApplicationCall>.() -> Unit,
): Route = route(path = path, name = name, method = method) { composable(body) }

@KtorDsl
public fun Route.composable(body: @Composable PipelineContext<Unit, ApplicationCall>.() -> Unit) {
    val routing = asRouting ?: error("Your route $this must have a parent Routing")
    handle {
        composable(routing = routing, resource = null) {
            body()
        }
    }
}

@KtorDsl
public inline fun <reified T : Any> Route.composable(noinline body: @Composable PipelineContext<Unit, ApplicationCall>.(T) -> Unit): Route {
    val routing = asRouting ?: error("Your route $this must have a parent Routing")
    return handle<T> { resource ->
        composable(routing = routing, resource = resource) {
            body(resource)
        }
    }
}

public inline fun <reified T : Any> Route.composable(
    method: RouteMethod,
    noinline body: @Composable PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
): Route {
    val routing = asRouting ?: error("Your route $this must have a parent Routing")
    return handle<T>(method = method) { resource ->
        composable(routing = routing, resource = resource) {
            body(resource)
        }
    }
}

public suspend fun <T> PipelineContext<Unit, ApplicationCall>.composable(
    routing: Routing,
    resource: T?,
    body: ComposeContent,
) {
    call.popped = false
    call.resource = resource
    call.content = body

    // Try clear restored flag on previous call
    val previousCall = routing.callStack.lastOrNull()
    if (previousCall?.restored == true) {
        previousCall.restored = false
    }

    if (call.shouldNeglect()) {
        return
    }

    // Clear pop call after each new routing call
    routing.poppedCall = null
    routing.popResult = null

    when (call.routeMethod) {
        RouteMethod.Push ->
            call.platformPush(routing) {
                routing.callStack.add(call)
            }
        RouteMethod.Replace ->
            call.platformReplace(routing) {
                routing.callStack.run {
                    removeLastOrNull()
                    add(call)
                }
            }
        RouteMethod.ReplaceAll ->
            call.platformReplaceAll(routing) {
                routing.callStack.run {
                    clear()
                    add(call)
                }
            }
        else ->
            error(
                "Compose needs a stack route method to work. You called a composable ${call.uri} " +
                    "using route method ${call.routeMethod} that is not supported",
            )
    }
}
