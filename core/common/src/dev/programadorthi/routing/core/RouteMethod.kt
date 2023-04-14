package dev.programadorthi.routing.core

public data class RouteMethod(val value: String) {

    public companion object {
        public val Pop: RouteMethod = RouteMethod("POP")
        public val Push: RouteMethod = RouteMethod("PUSH")
        public val Replace: RouteMethod = RouteMethod("REPLACE")
        public val ReplaceAll: RouteMethod = RouteMethod("REPLACE_ALL")

        public fun parse(method: String): RouteMethod {
            return when (method) {
                Pop.value -> Pop
                Push.value -> Push
                Replace.value -> Replace
                ReplaceAll.value -> ReplaceAll
                else -> RouteMethod(method)
            }
        }
    }
}
