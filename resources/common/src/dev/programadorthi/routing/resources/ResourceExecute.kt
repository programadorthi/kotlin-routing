package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.pipeline.execute
import kotlinx.coroutines.launch

public inline fun <reified T : Any> Routing.execute(resource: T) {
    val destination = application.href(resource)
    execute(
        ApplicationCall(
            application = application,
            uri = destination
        )
    )
}

public inline fun <reified T : Any> ApplicationCall.redirectTo(resource: T) {
    with(application) {
        launch {
            execute(
                ApplicationCall(
                    application = application,
                    uri = href(resource),
                )
            )
        }
    }
}
