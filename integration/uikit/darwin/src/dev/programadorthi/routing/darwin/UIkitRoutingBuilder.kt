package dev.programadorthi.routing.darwin

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.route
import io.ktor.utils.io.KtorDsl

@KtorDsl
public fun Route.controller(
    path: String,
    body: ApplicationCall.() -> UIKitUIViewController,
): Route = route(path = path, name = null) { controller(body = body, animated = false) }

@KtorDsl
public fun Route.controller(
    path: String,
    animated: Boolean = false,
    body: ApplicationCall.() -> UIKitUIViewController,
): Route = route(path = path, name = null) { controller(body = body, animated = animated) }

@KtorDsl
public fun Route.controller(
    path: String,
    name: String? = null,
    body: ApplicationCall.() -> UIKitUIViewController,
): Route = route(path = path, name = name) { controller(body = body, animated = false) }

@KtorDsl
public fun Route.controller(
    path: String,
    name: String? = null,
    animated: Boolean = false,
    body: ApplicationCall.() -> UIKitUIViewController,
): Route = route(path = path, name = name) { controller(body = body, animated = animated) }

@KtorDsl
public fun Route.controller(
    path: String,
    method: RouteMethod,
    body: ApplicationCall.() -> UIKitUIViewController,
): Route =
    route(path = path, name = null, method = method) {
        controller(
            body = body,
            animated = false,
        )
    }

@KtorDsl
public fun Route.controller(
    path: String,
    method: RouteMethod,
    animated: Boolean = false,
    body: ApplicationCall.() -> UIKitUIViewController,
): Route =
    route(path = path, name = null, method = method) {
        controller(
            body = body,
            animated = animated,
        )
    }

@KtorDsl
public fun Route.controller(
    path: String,
    method: RouteMethod,
    name: String? = null,
    body: ApplicationCall.() -> UIKitUIViewController,
): Route =
    route(path = path, name = name, method = method) {
        controller(
            body = body,
            animated = false,
        )
    }

@KtorDsl
public fun Route.controller(
    path: String,
    method: RouteMethod,
    name: String? = null,
    animated: Boolean = false,
    body: ApplicationCall.() -> UIKitUIViewController,
): Route =
    route(path = path, name = name, method = method) {
        controller(
            body = body,
            animated = animated,
        )
    }

@KtorDsl
public fun Route.controller(
    animated: Boolean = false,
    body: ApplicationCall.() -> UIKitUIViewController,
) {
    handle {
        val navigatorController = call.uiNavigationController
        val viewController = call.body()

        if (call.routeMethod == RouteMethod.Push) {
            navigatorController.pushViewController(
                viewController = viewController,
                animated = animated,
            )
            return@handle
        }

        val viewControllers = navigatorController.viewControllers().toMutableList()
        when (call.routeMethod) {
            RouteMethod.Replace -> viewControllers.removeLastOrNull()
            RouteMethod.ReplaceAll -> viewControllers.clear()
            else -> return@handle
        }
        viewControllers += viewController
        navigatorController.setViewControllers(
            viewControllers = viewControllers,
            animated = animated,
        )
    }
}
