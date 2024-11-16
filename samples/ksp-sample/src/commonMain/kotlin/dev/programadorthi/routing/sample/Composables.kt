package dev.programadorthi.routing.sample

import androidx.compose.runtime.Composable
import dev.programadorthi.routing.annotation.Body
import dev.programadorthi.routing.annotation.Path
import dev.programadorthi.routing.annotation.Route
import dev.programadorthi.routing.core.application.Application
import io.ktor.http.Parameters
import io.ktor.util.Attributes

@Route("/compose")
@Composable
fun compose() {
    println(">>>> I'm routing")
}

@Route(path = "/compose/{id}")
@Composable
fun compose(id: Double) {
    println(">>>> ID: $id")
}

@Route(path = "/composeNamed/{name}", name = "composeNamed")
@Composable
fun composeNamed(name: String) {
    println(">>>> name: $name")
}

@Route(path = "/composeCustom/{random}", name = "composeCustom")
@Composable
fun composeCustom(@Path("random") value: String) {
    println(">>>> value: $value")
}

@Route(path = "/composeOptional/{id?}")
@Composable
fun composeOptional(id: Char?) {
    println(">>>> Optional ID: $id")
}

@Route(path = "/composeTailcard/{param...}")
@Composable
fun composeTailcard(param: List<String>?) {
    println(">>>> Tailcard params: $param")
}

/*@Route(regex = ".+/hello")
@Composable
fun composeRegex1() {
    println(">>>> Routing with regex")
}*/

@Route("/compose-with-body")
@Composable
fun composeWithBody(@Body user: User) {
    println(">>>> with body $user")
}

@Route("/compose-with-null-body")
@Composable
fun composeWithNullBody(@Body user: User?) {
    println(">>>> null body $user")
}

@Route("/compose", method = "PUSH")
@Composable
fun composeByPushMethod() {
    println(">>>> I'm pushing a route")
}

@Route("/compose/{part1}/{part2}")
@Composable
fun composeMultiParameters(part1: Int, part2: String) {
    println(">>>> Parts: $part1 and $part2")
}

@Route("/compose-call/{part1}/{part2}")
@Composable
fun composeCallParameters(
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
