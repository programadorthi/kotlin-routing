package dev.programadorthi.routing.core

import dev.programadorthi.routing.annotation.Body
import dev.programadorthi.routing.annotation.Path
import dev.programadorthi.routing.annotation.Route
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.util.Attributes

data class User(
    val id: Int,
    val name: String,
)

@Route("/path")
fun execute() {
    println(">>>> I'm routing")
}

@Route(path = "/path/{id}")
fun execute(id: Double) {
    println(">>>> ID: $id")
}

@Route(path = "/named/{name}", name = "named")
fun named(name: String) {
    println(">>>> name: $name")
}

@Route(path = "/custom/{random}", name = "custom")
fun custom(
    @Path("random") value: String
) {
    println(">>>> value: $value")
}

@Route(path = "/optional/{id?}")
fun optional(id: Char?) {
    println(">>>> Optional ID: $id")
}

@Route(path = "/tailcard/{param...}")
fun tailcard(param: List<String>?) {
    println(">>>> Tailcard params: $param")
}

@Route(regex = ".+/hello")
fun regex1() {
    println(">>>> Routing with regex")
}

@Route(regex = "/(?<number>\\d+)")
fun regex2(number: Int) {
    println(">>>> Routing with regex to number: $number")
}

@Route("/with-body")
fun withBody(
    @Body user: User
) {
    println(">>>> with body $user")
}

@Route("/with-null-body")
fun withNullBody(
    @Body user: User?
) {
    println(">>>> null body $user")
}

@Route("/path", method = "PUSH")
fun byPushMethod() {
    println(">>>> I'm pushing a route")
}

@Route("/path/{part1}/{part2}")
fun multiParameters(part1: Int, part2: String) {
    println(">>>> Parts: $part1 and $part2")
}

@Route("/call")
fun call(call: ApplicationCall) {
    println(">>>> call: $call")
}

@Route("/call/{part1}/{part2}")
fun callParameters(
    application: Application,
    parameters: Parameters,
    attributes: Attributes,
) {
    println(
        """
        >>>> application: $application
        >>>> parameters: $parameters
        >>>> attributes: $attributes
        """.trimIndent()
    )
}
