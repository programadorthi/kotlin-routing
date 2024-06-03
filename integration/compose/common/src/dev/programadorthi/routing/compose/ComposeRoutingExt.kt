package dev.programadorthi.routing.compose

import dev.programadorthi.routing.compose.history.ComposeHistoryNeglectAttributeKey
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.call
import io.ktor.http.Parameters
import io.ktor.util.Attributes

internal expect fun Routing.popOnPlatform(
    result: Any? = null,
    fallback: () -> Unit,
)

public fun Routing.pop(result: Any? = null) {
    popOnPlatform(result = result) {
        if (callStack.size < 2) return@popOnPlatform

        poppedCall = callStack.removeLastOrNull()
        poppedCall?.popped = true
        poppedCall?.popResult = result

        val last = callStack.lastOrNull() ?: return@popOnPlatform
        if (last.content == null) {
            execute(last)
        }
    }
}

public fun Routing.push(
    path: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.Push,
        attributes = neglect.toAttributes(),
    )
}

public fun Routing.pushNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.Push,
        attributes = neglect.toAttributes(),
    )
}

public fun Routing.replace(
    path: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.Replace,
        attributes = neglect.toAttributes(),
    )
}

public fun Routing.replaceNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.Replace,
        attributes = neglect.toAttributes(),
    )
}

public fun Routing.replaceAll(
    path: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    call(
        uri = path,
        parameters = parameters,
        routeMethod = RouteMethod.ReplaceAll,
        attributes = neglect.toAttributes(),
    )
}

public fun Routing.replaceAllNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    call(
        name = name,
        parameters = parameters,
        routeMethod = RouteMethod.ReplaceAll,
        attributes = neglect.toAttributes(),
    )
}

private fun Boolean.toAttributes(): Attributes {
    val attributes = Attributes()
    attributes.put(ComposeHistoryNeglectAttributeKey, this)
    return attributes
}
