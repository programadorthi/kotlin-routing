package dev.programadorthi.routing.sample

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.annotation.Body
import dev.programadorthi.routing.annotation.Route

@Route("/screen")
class Screen1 : Screen {
    @Composable
    override fun Content() {}
}

@Route("/screen-object")
object Screen2 : Screen {
    @Composable
    override fun Content() {}
}

@Route("/screen/{id}")
class Screen3(id: Int) : Screen {
    @Composable
    override fun Content() {}
}

@Route("/screen/{name}")
class Screen4(name: String) : Screen {

    @Route("/screen/{age}")
    constructor(age: Int) : this("empty")

    @Composable
    override fun Content() {}
}

@Route("/screen-with-body")
class Screen5(@Body user: User) : Screen {

    @Composable
    override fun Content() {}
}
