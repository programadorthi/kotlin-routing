import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll
import dev.programadorthi.routing.javascript.pop
import kotlinx.browser.document
import kotlinx.html.button
import kotlinx.html.dom.create
import kotlinx.html.h1
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.p
import org.w3c.dom.HTMLElement

val page1: HTMLElement = createPage(
    h1Text = "You are on the page 1",
    nextPageName = "page 2",
    nextPath = "/page2",
)

val page2: HTMLElement = createPage(
    h1Text = "You are on the page 2",
    nextPageName = "page 3",
    nextPath = "/page3",
)

val page3: HTMLElement = createPage(
    h1Text = "You are on the page 3",
    nextPageName = "page 4",
    nextPath = "/page4",
)

val page4: HTMLElement = createPage(
    h1Text = "You are on the page 4",
    nextPageName = "page 1",
    nextPath = "/page1",
)

fun createPage(
    h1Text: String,
    nextPageName: String,
    nextPath: String,
): HTMLElement = document.create.div {
    h1 {
        +h1Text
    }
    p {
        button {
            +"Push $nextPageName"
            onClickFunction = {
                router.push(path = nextPath)
            }
        }
    }
    p {
        button {
            +"Replace with $nextPageName"
            onClickFunction = {
                router.replace(path = nextPath)
            }
        }
    }
    p {
        button {
            +"Replace all with $nextPageName"
            onClickFunction = {
                router.replaceAll(path = nextPath)
            }
        }
    }
    p {
        button {
            +"Pop the current page"
            onClickFunction = {
                router.pop()
            }
        }
    }
}