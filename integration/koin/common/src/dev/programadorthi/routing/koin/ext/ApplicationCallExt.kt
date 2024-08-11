/*
 * Copyright Koin the original author or authors.
 * https://github.com/InsertKoinIO/koin/blob/main/projects/ktor/koin-ktor/src/main/kotlin/org/koin/ktor/ext/ApplicationCallExt.kt
 */
package dev.programadorthi.routing.koin.ext

import dev.programadorthi.routing.core.application.ApplicationCall
import org.koin.core.Koin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/*
 * Ktor Koin extensions for ApplicationCall class
 *
 * @author Gopal Sharma
 */

/**
 * inject lazily given dependency
 * @param qualifier - bean name / optional
 * @param parameters
 */
public inline fun <reified T : Any> ApplicationCall.inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): Lazy<T> = lazy { get<T>(qualifier, parameters) }

/**
 * Retrieve given dependency for KoinComponent
 * @param qualifier - bean name / optional
 * @param parameters
 */
public inline fun <reified T : Any> ApplicationCall.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = getKoin().get<T>(qualifier, parameters)

/**
 * Retrieve given property for KoinComponent
 * @param key - key property
 */
public fun <T : Any> ApplicationCall.getProperty(key: String): T? = getKoin().getProperty(key)

/**
 * Retrieve given property for KoinComponent
 * give a default value if property is missing
 *
 * @param key - key property
 * @param defaultValue - default value if property is missing
 *
 */
public fun ApplicationCall.getProperty(
    key: String,
    defaultValue: String,
): String = getKoin().getProperty(key) ?: defaultValue

/**
 * Help work on ModuleDefinition
 */
public fun ApplicationCall.getKoin(): Koin = application.getKoin()
