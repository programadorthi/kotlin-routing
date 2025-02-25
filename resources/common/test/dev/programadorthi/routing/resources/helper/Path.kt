package dev.programadorthi.routing.resources.helper

import io.ktor.resources.Resource

@Resource("/path")
class Path {
    @Resource("{id}")
    class Id(val parent: Path = Path(), val id: Int)

    @Resource("/optional/{param?}")
    class Optional(val parent: Path = Path(), val param: String?)
}

@Resource("/parent")
class ParentRouting {
    @Resource("/child")
    class ChildRouting(val parent: ParentRouting = ParentRouting()) {
        @Resource("/destination")
        class Destination(val parent: ChildRouting = ChildRouting())
    }
}
