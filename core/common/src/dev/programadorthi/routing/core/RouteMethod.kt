package dev.programadorthi.routing.core

public interface RouteMethod {
    public val value: String

    public companion object {
        public val Empty: RouteMethod = RouteMethod("EMPTY")
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

public fun RouteMethod(value: String): RouteMethod = RouteMethodImpl(value)

private data class RouteMethodImpl(override val value: String) : RouteMethod
