package dev.programadorthi.routing.core

public data class StackRouteMethod(override val value: String) : RouteMethod {

    public companion object {
        public val Pop: StackRouteMethod = StackRouteMethod("POP")
        public val Push: StackRouteMethod = StackRouteMethod("PUSH")
        public val Replace: StackRouteMethod = StackRouteMethod("REPLACE")
        public val ReplaceAll: StackRouteMethod = StackRouteMethod("REPLACE_ALL")

        public fun parse(method: String): StackRouteMethod {
            return when (method) {
                Pop.value -> Pop
                Push.value -> Push
                Replace.value -> Replace
                ReplaceAll.value -> ReplaceAll
                else -> StackRouteMethod(method)
            }
        }
    }
}
