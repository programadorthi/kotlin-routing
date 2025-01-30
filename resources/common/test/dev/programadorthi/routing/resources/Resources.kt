package dev.programadorthi.routing.resources

import dev.programadorthi.routing.annotation.Body
import dev.programadorthi.routing.annotation.TypeSafeRoute
import dev.programadorthi.routing.resources.helper.Path

internal val invoked = mutableMapOf<String, List<Any?>>()

internal data class User(
    val id: Int,
    val name: String,
)

@TypeSafeRoute(Path::class)
fun execute() {
    invoked += "/path" to emptyList()
}

@TypeSafeRoute(Path.Id::class)
fun execute(pathId: Path.Id) {
    invoked += "/path/{id}" to listOf(pathId)
}

@TypeSafeRoute(Path::class, method = "PUSH")
fun executePush() {
    invoked += "/path-push" to emptyList()
}

@TypeSafeRoute(Path::class, method = "POST")
internal fun executePost(
    @Body user: User
) {
    invoked += "/path-post" to listOf(user)
}

@TypeSafeRoute(Path::class, method = "custom")
internal fun executeCustom(
    @Body user: User
) {
    invoked += "/path-custom" to listOf(user)
}

@TypeSafeRoute(Path.Optional::class)
internal fun executeOptional(optional: Path.Optional) {
    invoked += "/path-optional" to listOf(optional)
}
