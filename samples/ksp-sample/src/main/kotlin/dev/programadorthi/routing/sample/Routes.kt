package dev.programadorthi.routing.sample

import dev.programadorthi.routing.annotation.Route

@Route("/path")
fun execute() {
    println(">>>> I'm routing")
}

@Route("/path/{id}/{name}")
fun execute(id: Double, name: String) {
    println(">>>> ID: $id, name: $name")
}

class Routes {
    //@Route("/path")
    fun run() {
        println(">>>> I'm routing")
    }
}
