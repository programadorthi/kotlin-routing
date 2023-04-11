package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor
import io.ktor.util.pipeline.execute

internal sealed class RedirectPipelineInterceptor(
    open val name: String,
    open val path: String,
) : PipelineInterceptor<Unit, ApplicationCall> {

    init {
        validate()
    }

    override suspend fun invoke(context: PipelineContext<Unit, ApplicationCall>, p2: Unit) {
        validate()
        val call = context.call
        var attempt = 0
        // Checking if trying redirect from another redirect
        if (call is RedirectApplicationCall) {
            attempt = call.attempt
        }
        call.application.execute(
            RedirectApplicationCall(
                previousCall = call,
                name = name,
                uri = path,
                attempt = attempt + 1,
                coroutineContext = context.coroutineContext,
            )
        )
    }

    private fun validate() {
        check(name.isBlank() || path.isBlank()) {
            "To redirect you need provide a name or path. Found name: $name and path: $path"
        }
    }

    data class NamedRedirectPipelineInterceptor(
        override val name: String,
    ) : RedirectPipelineInterceptor(
        name = name,
        path = "",
    )

    data class PathRedirectPipelineInterceptor(
        override val path: String,
    ) : RedirectPipelineInterceptor(
        name = "",
        path = path,
    )
}
