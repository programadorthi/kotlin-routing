package dev.programadorthi.routing.resources.helper

import io.ktor.resources.Resource

@Resource("/path")
class Path {
    @Resource("{id}")
    class Id(val parent: Path = Path(), val id: Int)
}
