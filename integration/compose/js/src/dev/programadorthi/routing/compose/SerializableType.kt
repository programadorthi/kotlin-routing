package dev.programadorthi.routing.compose

@Suppress("UNCHECKED_CAST")
public actual fun <T> String.toSerializableType(): T = this as T
