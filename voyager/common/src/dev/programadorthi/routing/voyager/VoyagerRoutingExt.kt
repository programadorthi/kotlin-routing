package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.call
import io.ktor.http.Parameters

public fun Routing.canPop(): Boolean = application.voyagerNavigator.canPop

public fun Routing.pop() {
    application.voyagerNavigator.pop()
}

public fun Routing.popUntil(predicate: (Screen) -> Boolean) {
    application.voyagerNavigator.popUntil(predicate)
}

public fun Routing.push(
    path: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = VoyagerRouteMethod.Push,
    )
}

public fun Routing.pushNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = VoyagerRouteMethod.Push,
    )
}

public fun Routing.replace(
    path: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = VoyagerRouteMethod.Replace,
    )
}

public fun Routing.replaceNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = VoyagerRouteMethod.Replace,
    )
}

public fun Routing.replaceAll(
    path: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = VoyagerRouteMethod.ReplaceAll,
    )
}

public fun Routing.replaceAllNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = VoyagerRouteMethod.ReplaceAll,
    )
}