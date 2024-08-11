/*
 * Copyright Koin the original author or authors.
 * https://github.com/InsertKoinIO/koin/blob/main/projects/ktor/koin-ktor/src/main/kotlin/org/koin/ktor/ext/RouteExt.kt
 */
package dev.programadorthi.routing.koin.ext

import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.koin.KOIN_ATTRIBUTE_KEY
import org.koin.core.Koin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/*
 * Ktor Koin extensions for Routing class
 *
 * @author Arnaud Giuliani
 * @author Laurent Baresse
 */

/**
 * inject lazily given dependency
 * @param qualifier - bean name / optional
 * @param parameters
 */
public inline fun <reified T : Any> Route.inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): Lazy<T> = lazy { get<T>(qualifier, parameters) }

/**
 * Retrieve given dependency for KoinComponent
 * @param qualifier - bean name / optional
 * @param parameters
 */
public inline fun <reified T : Any> Route.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = getKoin().get<T>(qualifier, parameters)

/**
 * Retrieve given property for KoinComponent
 * @param key - key property
 */
public fun <T : Any> Route.getProperty(key: String): T? = getKoin().getProperty(key)

/**
 * Retrieve given property for KoinComponent
 * give a default value if property is missing
 *
 * @param key - key property
 * @param defaultValue - default value if property is missing
 *
 */
public fun Route.getProperty(
    key: String,
    defaultValue: String,
): String = getKoin().getProperty(key) ?: defaultValue

/**
 * Help work on ModuleDefinition
 */
public fun Route.getKoin(): Koin {
    val attrs =
        when {
            this is Routing -> application.attributes
            else -> attributes
        }
    // Is there an inner DI container for this Route ?
    val koin = attrs.getOrNull(KOIN_ATTRIBUTE_KEY)?.koin
    if (koin != null) {
        return koin
    }
    return parent?.getKoin()
        ?: error("No Koin instance started to [$this]. Use install(Koin) or startKoin()")
}
