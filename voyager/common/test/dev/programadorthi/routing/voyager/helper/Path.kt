package dev.programadorthi.routing.voyager.helper

import io.ktor.resources.Resource

@Resource("/path")
class Path {
    @Resource("{id}")
    class Id(val parent: Path = Path(), val id: Int)

    @Resource("/name")
    class Name(val parent: Path = Path())
}
