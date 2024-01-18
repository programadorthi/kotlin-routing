package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
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
        composable(routing) {
            body()
        }
    }
}

@KtorDsl
public inline fun <reified T : Any> Route.composable(noinline body: @Composable PipelineContext<Unit, ApplicationCall>.(T) -> Unit): Route {
    val routing = asRouting ?: error("Your route $this must have a parent Routing")
    return handle<T> { resource ->
        composable(routing) {
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
        composable(routing) {
            body(resource)
        }
    }
}

public fun PipelineContext<Unit, ApplicationCall>.composable(
    routing: Routing,
    body: @Composable () -> Unit,
) {
    routing.popResult = null // Clear pop result after each new routing call
    val stateList = routing.contentList
    when (call.routeMethod) {
        RouteMethod.Push -> stateList.add(body)
        RouteMethod.Replace -> {
            stateList.removeLastOrNull()
            stateList.add(body)
        }
        RouteMethod.ReplaceAll -> {
            stateList.clear()
            stateList.add(body)
        }

        else ->
            error(
                "Compose needs a stack route method to work. You called a composable ${call.uri} " +
                    "using route method ${call.routeMethod} that is not supported",
            )
    }
}
