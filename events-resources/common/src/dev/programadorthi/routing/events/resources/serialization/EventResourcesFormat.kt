package dev.programadorthi.routing.events.resources.serialization

import dev.programadorthi.routing.events.resources.Event
import io.ktor.http.Parameters
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * A format to (de)serialize resources instances
 */
@OptIn(ExperimentalSerializationApi::class)
public class EventResourcesFormat(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
) : SerialFormat {
    /**
     * A query parameter description
     */
    public data class Parameter(
        val name: String,
        val isOptional: Boolean,
    )

    /**
     * Builds a path pattern for a given [serializer]
     */
    public fun <T> encodeToPathPattern(serializer: KSerializer<T>): String {
        return serializer.descriptor.annotations.filterIsInstance<Event>().first().name
    }

    /**
     * Builds a description of query parameters for a given [serializer]
     */
    public fun <T> encodeToQueryParameters(serializer: KSerializer<T>): Set<Parameter> {
        val path = encodeToPathPattern(serializer)

        val allParameters = mutableSetOf<Parameter>()
        collectAllParameters(serializer.descriptor, allParameters)

        return allParameters
            .filterNot { (name, _) ->
                path.contains("{$name}") || path.contains("{$name?}") || path.contains("{$name...}")
            }
            .toSet()
    }

    private fun collectAllParameters(
        descriptor: SerialDescriptor,
        result: MutableSet<Parameter>,
    ) {
        descriptor.elementNames.forEach { name ->
            val index = descriptor.getElementIndex(name)
            val elementDescriptor = descriptor.getElementDescriptor(index)
            if (!elementDescriptor.isInline && elementDescriptor.kind is StructureKind.CLASS) {
                collectAllParameters(elementDescriptor, result)
            } else {
                result.add(Parameter(name, descriptor.isElementOptional(index)))
            }
        }
    }

    /**
     * Builds [Parameters] for a resource [T]
     */
    public fun <T> encodeToParameters(
        serializer: KSerializer<T>,
        value: T,
    ): Parameters {
        val encoder = ParametersEncoder(serializersModule)
        encoder.encodeSerializableValue(serializer, value)
        return encoder.parameters
    }

    /**
     * Builds a [T] resource instance from [parameters]
     */
    public fun <T> decodeFromParameters(
        deserializer: KSerializer<T>,
        parameters: Parameters,
    ): T {
        val input = ParametersDecoder(serializersModule, parameters, emptyList())
        return input.decodeSerializableValue(deserializer)
    }
}
