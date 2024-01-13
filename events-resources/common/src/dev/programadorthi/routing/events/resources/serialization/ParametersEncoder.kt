package dev.programadorthi.routing.events.resources.serialization

import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
internal class ParametersEncoder(
    override val serializersModule: SerializersModule,
) : AbstractEncoder() {
    private val parametersBuilder = ParametersBuilder()

    val parameters: Parameters
        get() = parametersBuilder.build()

    private lateinit var nextElementName: String

    override fun encodeValue(value: Any) {
        parametersBuilder.append(nextElementName, value.toString())
    }

    override fun encodeElement(
        descriptor: SerialDescriptor,
        index: Int,
    ): Boolean {
        if (descriptor.kind != StructureKind.LIST) {
            nextElementName = descriptor.getElementName(index)
        }
        return true
    }

    override fun encodeEnum(
        enumDescriptor: SerialDescriptor,
        index: Int,
    ) {
        encodeValue(enumDescriptor.getElementName(index))
    }

    override fun encodeNull() {
        // no op
    }
}
