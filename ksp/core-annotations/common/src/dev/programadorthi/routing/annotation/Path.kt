package dev.programadorthi.routing.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
public annotation class Path(
    val value: String
)
