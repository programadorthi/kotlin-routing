/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.programadorthi.routing.koin.ext

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.route
import io.ktor.util.pipeline.PipelineContext
import org.koin.core.Koin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

public inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): Lazy<T> = lazy { get<T>(qualifier, parameters) }

public inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = getKoin().get<T>(qualifier, parameters)

public fun <T : Any> PipelineContext<*, ApplicationCall>.getProperty(key: String): T? = getKoin().getProperty(key)

public fun PipelineContext<*, ApplicationCall>.getProperty(
    key: String,
    defaultValue: String,
): String = getKoin().getProperty(key) ?: defaultValue

public fun PipelineContext<*, ApplicationCall>.getKoin(): Koin {
    val route =
        requireNotNull(route()) {
            "Invalid context to get the koin instance: $call"
        }
    return route.getKoin()
}
