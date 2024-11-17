package dev.programadorthi.routing.annotation

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
)
public annotation class Route(
    val path: String = "",
    val name: String = "",
    val regex: String = "",
    val method: String = "",
)
