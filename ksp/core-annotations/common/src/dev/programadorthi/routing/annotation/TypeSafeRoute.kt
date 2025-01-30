package dev.programadorthi.routing.annotation

import kotlin.reflect.KClass

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
)
public annotation class TypeSafeRoute(
    val type: KClass<*>,
    val method: String = "",
)
