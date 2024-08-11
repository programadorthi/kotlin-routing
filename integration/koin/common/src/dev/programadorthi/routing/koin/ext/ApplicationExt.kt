/*
 * Copyright Koin the original author or authors.
 * https://github.com/InsertKoinIO/koin/blob/main/projects/ktor/koin-ktor/src/main/kotlin/org/koin/ktor/ext/ApplicationExt.kt
 */
package dev.programadorthi.routing.koin.ext

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.install
import dev.programadorthi.routing.core.application.pluginOrNull
import dev.programadorthi.routing.koin.KOIN_ATTRIBUTE_KEY
import dev.programadorthi.routing.koin.Koin
import org.koin.core.Koin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.KoinAppDeclaration

/*
 * Ktor Koin extensions
 *
 * @author Arnaud Giuliani
 * @author Laurent Baresse
 */

/**
 * Help work on ModuleDefinition
 */
public fun Application.getKoin(): Koin =
    attributes.getOrNull(KOIN_ATTRIBUTE_KEY)?.koin
        ?: error("No Koin instance started. Use install(Koin) or startKoin()")

/**
 * inject lazily given dependency
 * @param qualifier - bean name / optional
 * @param scope
 * @param parameters
 */
public inline fun <reified T : Any> Application.inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): Lazy<T> = lazy { get<T>(qualifier, parameters) }

/**
 * Retrieve given dependency for KoinComponent
 * @param qualifier - bean name / optional
 * @param scope
 * @param parameters
 */
public inline fun <reified T : Any> Application.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = getKoin().get<T>(qualifier, parameters)

/**
 * Retrieve given property for KoinComponent
 * @param key - key property
 */
public fun <T : Any> Application.getProperty(key: String): T? = getKoin().getProperty(key)

/**
 * Retrieve given property for KoinComponent
 * give a default value if property is missing
 *
 * @param key - key property
 * @param defaultValue - default value if property is missing
 *
 */
public fun Application.getProperty(
    key: String,
    defaultValue: String,
): String = getKoin().getProperty(key) ?: defaultValue

/**
 * Run extra koin configuration, like modules()
 */
public fun Application.koin(configuration: KoinAppDeclaration) {
    pluginOrNull(Koin)?.let {
        attributes.getOrNull(KOIN_ATTRIBUTE_KEY)?.apply(configuration)
    } ?: install(Koin) {
        setup(koinApplication = null, configuration = configuration)
    }
}
