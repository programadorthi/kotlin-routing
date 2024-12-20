package dev.programadorthi.routing.core

import dev.programadorthi.routing.annotation.Body
import dev.programadorthi.routing.annotation.Path
import dev.programadorthi.routing.annotation.Route
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import kotlin.to

internal val invoked = mutableMapOf<String, List<Any?>>()

internal data class User(
    val id: Int,
    val name: String,
)

@Route("/path")
fun execute() {
    invoked += "/path" to emptyList()
}

@Route(path = "/path/{id}")
fun execute(id: Double) {
    invoked += "/path/{id}" to listOf(id)
}

@Route(path = "/named/{name}", name = "named")
fun named(name: String) {
    invoked += "/named/{name}" to listOf(name)
}

@Route(path = "/custom/{random}", name = "custom")
fun custom(
    @Path("random") value: String
) {
    invoked += "/custom/{random}" to listOf(value)
}

@Route(path = "/optional/{id?}")
fun optional(id: Char?) {
    invoked += "/optional/{id?}" to listOf(id)
}

@Route(path = "/tailcard/{param...}")
fun tailcard(param: List<String>?) {
    invoked += "/tailcard/{param...}" to (param ?: emptyList())
}

@Route(regex = ".+/hello")
fun regex1() {
    invoked += ".+/hello" to listOf()
}

@Route(regex = "/(?<number>\\d+)")
fun regex2(number: Int) {
    invoked += "/(?<number>\\d+)" to listOf(number)
}

@Route("/with-body")
internal fun withBody(
    @Body user: User
) {
    invoked += "/with-body" to listOf(user)
}

@Route("/with-null-body")
internal fun withNullBody(
    @Body user: User?
) {
    invoked += "/with-null-body" to listOf(user)
}

@Route("/path", method = "PUSH")
fun byPushMethod() {
    invoked += "/path" to listOf("PUSH")
}

@Route("/path/{part1}/{part2}")
fun multiParameters(part1: Int, part2: String) {
    invoked += "/path/{part1}/{part2}" to listOf(part1, part2)
}

@Route("/call")
fun call(call: ApplicationCall) {
    invoked += "/call" to listOf(call)
}

@Route("/call/{part1}/{part2}")
fun callParameters(
    application: Application,
    parameters: Parameters,
    attributes: Attributes,
) {
    invoked += "/call/{part1}/{part2}" to listOf(application, parameters, attributes)
}
