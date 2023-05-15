package dev.programadorthi.routing.events.resources

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.BaseApplicationPlugin
import io.ktor.util.AttributeKey

/**
 * Adds support for type-safe event using [EventResourcesConfiguration].
 *
 * Example:
 * ```kotlin
 * @Event("event_name")
 * class DoSomething
 *
 * routing {
 *   event<DoSomething> { doSomething ->
 *     // handle the event
 *   }
 * }
 * ```
 */
public object EventResources :
    BaseApplicationPlugin<Application, EventResourcesConfiguration.Configuration, EventResourcesConfiguration> {

    override val key: AttributeKey<EventResourcesConfiguration> = AttributeKey("EventResourcesConfiguration")

    override fun install(
        pipeline: Application,
        configure: EventResourcesConfiguration.Configuration.() -> Unit
    ): EventResourcesConfiguration {
        val configuration = EventResourcesConfiguration.Configuration().apply(configure)
        return EventResourcesConfiguration(configuration)
    }
}
