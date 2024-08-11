/*
 * Copyright Koin the original author or authors.
 * https://github.com/InsertKoinIO/koin/blob/main/projects/ktor/koin-ktor/src/main/kotlin/org/koin/ktor/ext/RoutingExt.kt
 */
package dev.programadorthi.routing.koin.ext

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
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
public inline fun <reified T : Any> Routing.inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): Lazy<T> = lazy { get<T>(qualifier, parameters) }

/**
 * Retrieve given dependency for KoinComponent
 * @param qualifier - bean name / optional
 * @param parameters
 */
public inline fun <reified T : Any> Routing.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = getKoin().get<T>(qualifier, parameters)

/**
 * Retrieve given property for KoinComponent
 * @param key - key property
 */
public fun <T : Any> Routing.getProperty(key: String): T? = getKoin().getProperty(key)

/**
 * Retrieve given property for KoinComponent
 * give a default value if property is missing
 *
 * @param key - key property
 * @param defaultValue - default value if property is missing
 *
 */
public inline fun <reified T> Routing.getProperty(
    key: String,
    defaultValue: T,
): T = getKoin().getProperty(key) ?: defaultValue

/**
 * Help work on ModuleDefinition
 */
public fun Routing.getKoin(): Koin = application.getKoin()
