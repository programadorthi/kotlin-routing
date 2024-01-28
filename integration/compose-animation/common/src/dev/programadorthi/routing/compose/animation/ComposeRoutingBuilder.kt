package dev.programadorthi.routing.compose.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import dev.programadorthi.routing.compose.composable
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
    enterTransition: Animation<EnterTransition>? = null,
    exitTransition: Animation<ExitTransition>? = null,
    popEnterTransition: Animation<EnterTransition>? = enterTransition,
    popExitTransition: Animation<ExitTransition>? = exitTransition,
    body: @Composable PipelineContext<Unit, ApplicationCall>.() -> Unit,
): Route =
    route(path = path, name = name) {
        composable(
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
            body = body,
        )
    }

@KtorDsl
public fun Route.composable(
    path: String,
    method: RouteMethod,
    name: String? = null,
    enterTransition: Animation<EnterTransition>? = null,
    exitTransition: Animation<ExitTransition>? = null,
    popEnterTransition: Animation<EnterTransition>? = enterTransition,
    popExitTransition: Animation<ExitTransition>? = exitTransition,
    body: @Composable PipelineContext<Unit, ApplicationCall>.() -> Unit,
): Route =
    route(path = path, name = name, method = method) {
        composable(
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
            body = body,
        )
    }

@KtorDsl
public fun Route.composable(
    enterTransition: Animation<EnterTransition>? = null,
    exitTransition: Animation<ExitTransition>? = null,
    popEnterTransition: Animation<EnterTransition>? = enterTransition,
    popExitTransition: Animation<ExitTransition>? = exitTransition,
    body: @Composable PipelineContext<Unit, ApplicationCall>.() -> Unit,
) {
    val routing = asRouting ?: error("Your route $this must have a parent Routing")
    handle {
        composable<Nothing>(
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
            routing = routing,
            bodyComposable = body,
        )
    }
}

@KtorDsl
public inline fun <reified T : Any> Route.composable(
    noinline enterTransition: Animation<EnterTransition>? = null,
    noinline exitTransition: Animation<ExitTransition>? = null,
    noinline popEnterTransition: Animation<EnterTransition>? = enterTransition,
    noinline popExitTransition: Animation<ExitTransition>? = exitTransition,
    crossinline body: @Composable PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
): Route {
    val routing = asRouting ?: error("Your route $this must have a parent Routing")
    return handle<T> { resource ->
        composable(
            routing = routing,
            resource = resource,
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
        ) {
            body(resource)
        }
    }
}

public inline fun <reified T : Any> Route.composable(
    method: RouteMethod,
    noinline enterTransition: Animation<EnterTransition>? = null,
    noinline exitTransition: Animation<ExitTransition>? = null,
    noinline popEnterTransition: Animation<EnterTransition>? = enterTransition,
    noinline popExitTransition: Animation<ExitTransition>? = exitTransition,
    crossinline body: @Composable PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
): Route {
    val routing = asRouting ?: error("Your route $this must have a parent Routing")
    return handle<T>(method = method) { resource ->
        composable(
            routing = routing,
            resource = resource,
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
        ) {
            body(resource)
        }
    }
}

public fun <T> PipelineContext<Unit, ApplicationCall>.composable(
    routing: Routing,
    resource: T? = null,
    enterTransition: Animation<EnterTransition>? = null,
    exitTransition: Animation<ExitTransition>? = null,
    popEnterTransition: Animation<EnterTransition>? = enterTransition,
    popExitTransition: Animation<ExitTransition>? = exitTransition,
    bodyComposable: @Composable PipelineContext<Unit, ApplicationCall>.() -> Unit,
) {
    call.enterTransition = enterTransition
    call.exitTransition = exitTransition
    call.popEnterTransition = popEnterTransition
    call.popExitTransition = popExitTransition

    composable(
        routing = routing,
        resource = resource,
        body = {
            bodyComposable()
        },
    )
}
