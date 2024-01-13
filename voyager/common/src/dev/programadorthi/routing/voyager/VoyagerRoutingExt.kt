package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.call
import io.ktor.http.Parameters

public fun Routing.canPop(): Boolean = application.voyagerNavigator.canPop

public fun Routing.pop(result: Any? = null) {
    val navigator = application.voyagerNavigator
    if (navigator.pop()) {
        navigator.trySendPopResult(result)
    }
}

public fun Routing.popUntil(
    result: Any? = null,
    predicate: (Screen) -> Boolean,
) {
    val navigator = application.voyagerNavigator
    if (navigator.popUntil(predicate)) {
        navigator.trySendPopResult(result)
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
private fun <T> Navigator.trySendPopResult(result: T?) {
    if (result == null) return
    // Safe here because navigator pop() call is sync by default and last item is the previous screen
    val currentScreen = lastItemOrNull as? VoyagerRoutingPopResult<T>
    currentScreen?.onResult(result)
}
