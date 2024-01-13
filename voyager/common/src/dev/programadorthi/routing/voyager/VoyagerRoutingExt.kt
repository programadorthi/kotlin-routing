package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.call
import io.ktor.http.Parameters

public fun Routing.canPop(): Boolean = application.voyagerNavigator.canPop

public fun Routing.pop(parameters: Parameters = Parameters.Empty) {
    val navigator = application.voyagerNavigator
    if (navigator.pop()) {
        navigator.trySendPopResult(parameters)
    }
}

public fun Routing.popUntil(
    parameters: Parameters = Parameters.Empty,
    predicate: (Screen) -> Boolean
) {
    val navigator = application.voyagerNavigator
    if (navigator.popUntil(predicate)) {
        navigator.trySendPopResult(parameters)
    }
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

@Suppress("UNCHECKED_CAST")
private fun Navigator.trySendPopResult(
    parameters: Parameters
) {
    // Safe here because navigator pop() call is sync by default and last item is the previous screen
    val currentScreen = lastItemOrNull as? VoyagerRoutingPopResult<Parameters>
    currentScreen?.onResult(parameters)
}
