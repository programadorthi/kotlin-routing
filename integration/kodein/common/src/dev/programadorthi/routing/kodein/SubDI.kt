/*
 * Copyright Kodein the original author or authors.
 * https://github.com/kosi-libs/Kodein/blob/master/framework/ktor/kodein-di-framework-ktor-server-jvm/src/main/kotlin/org/kodein/di/ktor/subs.kt
 */
package dev.programadorthi.routing.kodein

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import org.kodein.di.Copy
import org.kodein.di.DI
import org.kodein.di.subDI

/**
 * Extend the nearest [DI] container, Global (from the Application) or Local (from a parent)
 */
public inline fun Route.subDI(
    allowSilentOverride: Boolean = false,
    copy: Copy = Copy.NonCached,
    crossinline init: DI.MainBuilder.() -> Unit,
) {
    // Get any DI container in the parent # avoid infinite loop / StackOverflowError
    val parentDI = parent?.closestDI() ?: closestDI { application }
    val attrs =
        when {
            this is Routing -> application.attributes
            else -> attributes
        }
    attrs.put(KodeinDIKey, subDI(parentDI, allowSilentOverride, copy, init))
}
