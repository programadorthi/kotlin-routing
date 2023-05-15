package dev.programadorthi.routing.events.resources

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MetaSerializable
import kotlinx.serialization.SerialInfo

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
@MetaSerializable
public annotation class Event(val name: String)
