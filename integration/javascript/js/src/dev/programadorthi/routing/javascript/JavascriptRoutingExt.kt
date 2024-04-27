package dev.programadorthi.routing.javascript

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.call
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import kotlinx.browser.window

public fun Routing.pop() {
    if (application.historyMode != JavascriptRoutingHistoryMode.Memory) {
        window.history.go(-1)
        return
    }
    val callStack = application.callStack
    if (callStack.size < 2) return
    // Remove actual visible
    callStack.removeLastOrNull()
    // Call previous to be showed
    val lastCall = callStack.lastOrNull() ?: return
    lastCall.neglect = true // Avoid putting on the stack again
    execute(lastCall)
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
    attributes.put(JavascriptNeglectAttributeKey, this)
    return attributes
}
