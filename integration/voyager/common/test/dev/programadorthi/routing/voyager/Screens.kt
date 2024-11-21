package dev.programadorthi.routing.voyager

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.annotation.Body
import dev.programadorthi.routing.annotation.Route

internal val invoked = mutableMapOf<String, List<Any?>>()

internal data class User(
    val id: Int,
    val name: String,
)

@Route("/screen")
class Screen1 : Screen {
    @Composable
    override fun Content() {
        invoked += "/screen" to emptyList()
    }
}

@Route("/screen-object")
object Screen2 : Screen {
    @Composable
    override fun Content() {
        invoked += "/screen-object" to emptyList()
    }
}

@Route("/screen/{id}")
class Screen3(val id: Int) : Screen {
    @Composable
    override fun Content() {
        invoked += "/screen/{id}" to listOf(id)
    }
}

@Route("/screen-with-name/{name}")
class Screen4(val name: String) : Screen {
    private var age: Int = -1

    @Route("/screen-with-age/{age}")
    constructor(age: Int) : this("") {
        this.age = age
    }

    @Composable
    override fun Content() {
        invoked += "/screen-with-name/{name}" to listOf(name)
        invoked += "/screen-with-age/{age}" to listOf(age)
    }
}

@Route("/screen-with-body")
internal class Screen5(@Body val user: User) : Screen {

    @Composable
    override fun Content() {
        invoked += "/screen-with-body" to listOf(user)
    }
}
