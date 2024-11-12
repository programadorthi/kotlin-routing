package dev.programadorthi.routing.sample

import dev.programadorthi.routing.annotation.Route

@Route("/path")
fun execute() {
    println(">>>> I'm routing")
}
