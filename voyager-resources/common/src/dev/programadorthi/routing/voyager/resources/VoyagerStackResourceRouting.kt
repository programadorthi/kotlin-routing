package dev.programadorthi.routing.voyager.resources

import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.resources.pop
import dev.programadorthi.routing.resources.push
import dev.programadorthi.routing.resources.replace
import dev.programadorthi.routing.resources.replaceAll
import dev.programadorthi.routing.voyager.checkPluginInstalled
import dev.programadorthi.routing.voyager.voyagerNavigatorManager
import io.ktor.util.pipeline.PipelineContext

/**
 * Registers a typed handler [body] for any [StackRouteMethod] resource defined by the [T] class.
 *
 * A class [T] **must** be annotated with [io.ktor.resources.Resource].
 *
 * @param body receives an instance of the typed resource [T] as the first parameter.
 */
public inline fun <reified T : Any> Route.screen(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Screen
): Route {
    checkPluginInstalled()

    push<T> { resource ->
        val screen = body(this, resource)
        call.voyagerNavigatorManager.push(screen)
    }

    replace<T> { resource ->
        val screen = body(this, resource)
        call.voyagerNavigatorManager.replace(screen = screen, replaceAll = false)
    }

    replaceAll<T> { resource ->
        val screen = body(this, resource)
        call.voyagerNavigatorManager.replace(screen = screen, replaceAll = true)
    }

    pop<T> {
        call.voyagerNavigatorManager.pop()
    }

    return this
}
