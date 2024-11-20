package dev.programadorthi.routing.core

import io.ktor.http.Parameters

public fun Routing.push(path: String) {
    push(path = path, parameters = Parameters.Empty)
}

public fun Routing.push(
    path: String,
    parameters: Parameters,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.Push,
    )
}

public fun Routing.pushNamed(name: String) {
    pushNamed(name = name, parameters = Parameters.Empty)
}

public fun Routing.pushNamed(
    name: String,
    parameters: Parameters,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.Push,
    )
}

public fun <T : Any> Routing.pushWithBody(
    path: String,
    body: T,
) {
    pushWithBody(
        path = path,
        parameters = Parameters.Empty,
        body = body,
    )
}

public fun <T : Any> Routing.pushWithBody(
    path: String,
    parameters: Parameters,
    body: T,
) {
    callWithBody(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.Push,
        body = body,
    )
}

public fun <T : Any> Routing.pushNamedWithBody(
    name: String,
    body: T,
) {
    pushNamedWithBody(
        name = name,
        parameters = Parameters.Empty,
        body = body,
    )
}

public fun <T : Any> Routing.pushNamedWithBody(
    name: String,
    parameters: Parameters,
    body: T,
) {
    callWithBody(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.Push,
        body = body,
    )
}

public fun Routing.replace(path: String) {
    replace(
        path = path,
        parameters = Parameters.Empty,
    )
}

public fun Routing.replace(
    path: String,
    parameters: Parameters,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.Replace,
    )
}

public fun Routing.replaceNamed(name: String) {
    replaceNamed(
        name = name,
        parameters = Parameters.Empty,
    )
}

public fun Routing.replaceNamed(
    name: String,
    parameters: Parameters,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.Replace,
    )
}

public fun <T : Any> Routing.replaceWithBody(
    path: String,
    body: T,
) {
    replaceWithBody(
        path = path,
        parameters = Parameters.Empty,
        body = body,
    )
}

public fun <T : Any> Routing.replaceWithBody(
    path: String,
    parameters: Parameters,
    body: T,
) {
    callWithBody(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.Replace,
        body = body,
    )
}

public fun <T : Any> Routing.replaceNamedWithBody(
    name: String,
    body: T,
) {
    replaceNamedWithBody(
        name = name,
        parameters = Parameters.Empty,
        body = body,
    )
}

public fun <T : Any> Routing.replaceNamedWithBody(
    name: String,
    parameters: Parameters,
    body: T,
) {
    callWithBody(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.Replace,
        body = body,
    )
}

public fun Routing.replaceAll(path: String) {
    replaceAll(
        path = path,
        parameters = Parameters.Empty,
    )
}

public fun Routing.replaceAll(
    path: String,
    parameters: Parameters,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.ReplaceAll,
    )
}

public fun Routing.replaceAllNamed(name: String) {
    replaceAllNamed(
        name = name,
        parameters = Parameters.Empty,
    )
}

public fun Routing.replaceAllNamed(
    name: String,
    parameters: Parameters,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.ReplaceAll,
    )
}

public fun <T : Any> Routing.replaceAllWithBody(
    path: String,
    body: T,
) {
    replaceAllWithBody(
        path = path,
        parameters = Parameters.Empty,
        body = body,
    )
}

public fun <T : Any> Routing.replaceAllWithBody(
    path: String,
    parameters: Parameters,
    body: T,
) {
    callWithBody(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.ReplaceAll,
        body = body,
    )
}

public fun <T : Any> Routing.replaceAllNamedWithBody(
    name: String,
    body: T,
) {
    replaceAllNamedWithBody(
        name = name,
        parameters = Parameters.Empty,
        body = body,
    )
}

public fun <T : Any> Routing.replaceAllNamedWithBody(
    name: String,
    parameters: Parameters,
    body: T,
) {
    callWithBody(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.ReplaceAll,
        body = body,
    )
}
