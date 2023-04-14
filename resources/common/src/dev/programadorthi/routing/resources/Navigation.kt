package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.routing

public inline fun <reified T : Any> Route.push(resource: T) {
    val destination = application.href(resource)
    routing().push(path = destination)
}

public inline fun <reified T : Any> Route.replace(resource: T) {
    val destination = application.href(resource)
    routing().replace(path = destination)
}

public inline fun <reified T : Any> Route.replaceAll(resource: T) {
    val destination = application.href(resource)
    routing().replaceAll(path = destination)
}
