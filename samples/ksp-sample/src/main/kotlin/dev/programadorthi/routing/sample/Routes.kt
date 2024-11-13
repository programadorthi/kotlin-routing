package dev.programadorthi.routing.sample

import dev.programadorthi.routing.annotation.Path
import dev.programadorthi.routing.annotation.Route

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
fun custom(@Path("random") value: String) {
    println(">>>> value: $value")
}

@Route(path = "/optional/{id?}")
fun optional(id: Char?) {
    println(">>>> Optional ID: $id")
}

class Routes {
    //@Route("/path")
    fun run() {
        println(">>>> I'm routing")
    }
}
