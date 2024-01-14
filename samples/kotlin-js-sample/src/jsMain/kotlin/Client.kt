import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.javascript.jsRoute
import dev.programadorthi.routing.javascript.render
import kotlinx.browser.document
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.h1
import kotlinx.html.js.onClickFunction

val router = routing {
    jsRoute(path = "/page1") {
        page1
    }

    jsRoute(path = "/page2") {
        page2
    }

    jsRoute(path = "/page3") {
        page3
    }
}

fun main() {
    render(
        routing = router,
        root = document.getElementById("root") ?: document.create.div(),
        initial = document.create.h1 {
            +"I am the initial content"
            onClickFunction = {
                router.push(path = "/page1")
            }
        }
    )
}


