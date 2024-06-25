package dev.programadorthi.routing.voyager

@Suppress("UNCHECKED_CAST")
public actual fun <T> String.toSerializableType(): T = this as T
