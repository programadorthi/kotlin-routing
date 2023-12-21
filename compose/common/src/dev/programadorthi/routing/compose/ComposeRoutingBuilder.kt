package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.application
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.previousCall
import dev.programadorthi.routing.core.route
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.execute

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
public fun Route.composable(
    body: @Composable PipelineContext<Unit, ApplicationCall>.() -> Unit,
) {
    handle {
        // Avoiding recompose same content on a popped call
        if (call.routeMethod != StackRouteMethod.Pop) {
            call.content = { body(this) }
        } else {
            // Checking for previous ApplicationCall and redirecting to it
            val previous = previousCall()
            if (previous != null) {
                application.execute(previous)
            }
        }
    }
}
