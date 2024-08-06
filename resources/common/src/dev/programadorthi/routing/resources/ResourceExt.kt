package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.plugin
import kotlinx.serialization.serializer

public inline fun <reified T : Any> Routing.canHandleByResource(lookUpOnParent: Boolean = false): Boolean {
    val resourceFormat = plugin(Resources).resourcesFormat
    val path = resourceFormat.encodeToPathPattern(serializer<T>())
    return canHandleByPath(path = path, lookUpOnParent = lookUpOnParent)
}

public inline fun <reified T : Any> Routing.canHandleByResource(
    method: RouteMethod,
    lookUpOnParent: Boolean = false,
): Boolean {
    val resourceFormat = plugin(Resources).resourcesFormat
    val path = resourceFormat.encodeToPathPattern(serializer<T>())
    return canHandleByPath(path = path, method = method, lookUpOnParent = lookUpOnParent)
}
