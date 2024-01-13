package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.isPop
import dev.programadorthi.routing.core.method
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.resources.handle
import dev.programadorthi.routing.resources.resource
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.serializer

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
    handle {
        composable {
            body(this)
        }
    }
}

@KtorDsl
public inline fun <reified T : Any> Route.composable(noinline body: @Composable PipelineContext<Unit, ApplicationCall>.(T) -> Unit): Route =
    resource<T> {
        handle(serializer<T>()) { value ->
            composable {
                body(value)
            }
        }
    }

public inline fun <reified T : Any> Route.composable(
    method: RouteMethod,
    noinline body: @Composable PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
): Route {
    lateinit var builtRoute: Route
    resource<T> {
        builtRoute =
            method(method) {
                handle(serializer<T>()) { value ->
                    composable {
                        body(value)
                    }
                }
            }
    }
    return builtRoute
}

public fun PipelineContext<Unit, ApplicationCall>.composable(body: @Composable () -> Unit) {
    // Avoiding recompose same content on a popped call
    if (call.isPop()) return

    call.content = body
}
