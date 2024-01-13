package dev.programadorthi.routing.events.resources

import dev.programadorthi.routing.events.resources.serialization.EventResourcesFormat
import io.ktor.util.KtorDsl
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * Event Resources plugin instance.
 */
public class EventResourcesConfiguration(configuration: Configuration) {
    /**
     * The format instance used to (de)serialize resources instances
     */
    public val resourcesFormat: EventResourcesFormat = EventResourcesFormat(configuration.serializersModule)

    /**
     * Configuration for the Resources plugin instance.
     */
    @KtorDsl
    public class Configuration {
        /**
         * [SerializersModule] used to (de)serialize the Resource instances.
         */
        public var serializersModule: SerializersModule = EmptySerializersModule()
    }
}
