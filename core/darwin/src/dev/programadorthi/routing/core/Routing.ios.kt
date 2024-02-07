package dev.programadorthi.routing.core

import io.ktor.http.Parameters
import io.ktor.util.Attributes

public fun Routing.callUri(uri: String) {
    call(uri = uri)
}

public fun Routing.callUri(
    uri: String,
    routeMethod: RouteMethod,
) {
    call(uri = uri, routeMethod = routeMethod)
}

public fun Routing.callUri(
    uri: String,
    attributes: Attributes,
) {
    call(uri = uri, attributes = attributes)
}

public fun Routing.callUri(
    uri: String,
    routeMethod: RouteMethod,
    attributes: Attributes,
) {
    call(uri = uri, routeMethod = routeMethod, attributes = attributes)
}

public fun Routing.callUri(
    uri: String,
    parameters: Parameters,
) {
    call(uri = uri, parameters = parameters)
}

public fun Routing.callUri(
    uri: String,
    routeMethod: RouteMethod,
    parameters: Parameters,
) {
    call(uri = uri, routeMethod = routeMethod, parameters = parameters)
}

public fun Routing.callUri(
    uri: String,
    attributes: Attributes,
    parameters: Parameters,
) {
    call(uri = uri, attributes = attributes, parameters = parameters)
}

public fun Routing.callUri(
    uri: String,
    routeMethod: RouteMethod,
    attributes: Attributes,
    parameters: Parameters,
) {
    call(uri = uri, routeMethod = routeMethod, attributes = attributes, parameters = parameters)
}

public fun Routing.callName(name: String) {
    call(name = name)
}

public fun Routing.callName(
    name: String,
    routeMethod: RouteMethod,
) {
    call(name = name, routeMethod = routeMethod)
}

public fun Routing.callName(
    name: String,
    attributes: Attributes,
) {
    call(name = name, attributes = attributes)
}

public fun Routing.callName(
    name: String,
    routeMethod: RouteMethod,
    attributes: Attributes,
) {
    call(name = name, routeMethod = routeMethod, attributes = attributes)
}

public fun Routing.callName(
    name: String,
    parameters: Parameters,
) {
    call(name = name, parameters = parameters)
}

public fun Routing.callName(
    name: String,
    routeMethod: RouteMethod,
    parameters: Parameters,
) {
    call(name = name, routeMethod = routeMethod, parameters = parameters)
}

public fun Routing.callName(
    name: String,
    attributes: Attributes,
    parameters: Parameters,
) {
    call(name = name, attributes = attributes, parameters = parameters)
}

public fun Routing.callName(
    name: String,
    routeMethod: RouteMethod,
    attributes: Attributes,
    parameters: Parameters,
) {
    call(name = name, routeMethod = routeMethod, attributes = attributes, parameters = parameters)
}
