package dev.programadorthi.routing.sample

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

class Routes {
    //@Route("/path")
    fun run() {
        println(">>>> I'm routing")
    }
}
