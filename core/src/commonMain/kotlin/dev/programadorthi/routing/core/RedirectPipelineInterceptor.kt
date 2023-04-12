package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.http.Parameters
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor
import io.ktor.util.pipeline.execute
import kotlinx.coroutines.launch

internal sealed class RedirectPipelineInterceptor(
    open val name: String,
    open val path: String,
    open val pathParameters: Parameters,
) : PipelineInterceptor<Unit, ApplicationCall> {

    override suspend fun invoke(context: PipelineContext<Unit, ApplicationCall>, p2: Unit) {
        validate()
        val call = context.call
        with(call.application) {
            launch {
                execute(
                    RedirectApplicationCall(
                        previousCall = call,
                        name = name,
                        uri = path,
                        coroutineContext = context.coroutineContext,
                        pathParameters = pathParameters,
                    )
                )
            }
        }
    }

    private fun validate() {
        check(name.isBlank() || path.isBlank()) {
            "To redirect you need provide a name or path. Found name: $name and path: $path"
        }
    }

    data class NamedRedirectPipelineInterceptor(
        override val name: String,
        override val pathParameters: Parameters = Parameters.Empty,
    ) : RedirectPipelineInterceptor(
        name = name,
        path = "",
        pathParameters = pathParameters,
    )

    data class PathRedirectPipelineInterceptor(
        override val path: String,
    ) : RedirectPipelineInterceptor(
        name = "",
        path = path,
        pathParameters = Parameters.Empty,
    )
}
