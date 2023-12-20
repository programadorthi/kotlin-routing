package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.route
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext

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
        call.content = { body(this) }
    }
}
