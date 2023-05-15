import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.StackRouting
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.routing
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.util.toMap
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.channels.Channel
import kotlinx.dom.clear
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.w3c.dom.PopStateEvent

const val ROUTE_METHOD_KEY = "route-method"
const val ROUTE_URI_KEY = "route-uri"

var initialPath = ""
var replacingAll = false
var channel: Channel<Parameters>? = null

fun ApplicationCall.toData(): String {
    val other = parametersOf(
        ROUTE_METHOD_KEY to listOf(routeMethod.value),
        ROUTE_URI_KEY to listOf(uri),
    )
    val serializer = serializer<Map<String, List<String>>>()
    return Json.encodeToString(serializer, other.toMap())
}

fun PopStateEvent.stateToParameters(): Parameters {
    val other = state as? String ?: return Parameters.Empty
    val serializer = serializer<Map<String, List<String>>>()
    val map = Json.decodeFromString(serializer, other)
    return parametersOf(map)
}

val router = routing {
    install(StackRouting)

    browser(path = "/page1") {
        document.body?.run {
            clear()
            appendChild(page1)
        }
    }

    browser(path = "/page2") {
        document.body?.run {
            clear()
            appendChild(page2)
        }
    }

    browser(path = "/page3") {
        document.body?.run {
            clear()
            appendChild(page3)
        }
    }
}

fun main() {
    window.onload = {
        initialPath = "/page1"
        router.replace(path = initialPath)
    }

    window.onpopstate = { event ->
        val parameters = event.stateToParameters()
        if (replacingAll) {
            channel?.trySend(parameters)
        } else {
            val methodName = parameters[ROUTE_METHOD_KEY] ?: ""
            val method = StackRouteMethod.parse(methodName)
            val path = document.location?.run {
                "$pathname$search"
            } ?: initialPath

            if (method == StackRouteMethod.Push) {
                // TODO: How to not neglect when routing stack is empty and browser history no?
                router.push(path = path, neglect = true)
            } else {
                router.replace(path = path, neglect = true)
            }
        }
    }
}


