import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.core.stackNeglect
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineInterceptor
import kotlinx.browser.window
import kotlinx.coroutines.channels.Channel

@KtorDsl
public fun Route.browser(
    path: String,
    name: String? = null,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route = route(path = path, name = name) {
    push {
        body(this, Unit)
        if (!call.stackNeglect) {
            window.history.pushState(
                data = call.toData(),
                title = name ?: "",
                url = call.uri,
            )
        }
    }

    replace {
        body(this, Unit)
        if (!call.stackNeglect) {
            window.history.replaceState(
                data = call.toData(),
                title = name ?: "",
                url = call.uri,
            )
        }
    }

    replaceAll {
        body(this, Unit)
        if (!call.stackNeglect) {
            if (window.history.length > 1) {
                replacingAll = true
                channel = Channel(5)
                while (true) {
                    window.history.replaceState(
                        data = null,
                        title = "",
                        url = null,
                    )
                    window.history.back() // Triggering onpopstate

                    val parameters = channel?.receive()
                    if (parameters?.get(ROUTE_URI_KEY) == initialPath) break
                }
            }
            replacingAll = false

            channel?.close()
            channel = null

            window.history.replaceState(
                data = call.toData(),
                title = name ?: "",
                url = call.uri,
            )

            initialPath = call.uri
        }
    }

    pop {
        // TODO: avoid create a history navigation too
        if (!call.stackNeglect) {
            window.history.back()
        }
    }
}