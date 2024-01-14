package dev.programadorthi.routing.core

import io.ktor.http.Parameters

public fun Routing.push(
    path: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.Push,
    )
}

public fun Routing.pushNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.Push,
    )
}

public fun Routing.replace(
    path: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.Replace,
    )
}

public fun Routing.replaceNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.Replace,
    )
}

public fun Routing.replaceAll(
    path: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.ReplaceAll,
    )
}

public fun Routing.replaceAllNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.ReplaceAll,
    )
}
