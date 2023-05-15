import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replaceAll
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.h1
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.p
import org.w3c.dom.HTMLElement

val page1: HTMLElement = createPage(
    h1Text = "You are in the page 1",
    pText = "Click here to go to page 2",
    action = {
        router.push(path = "/page2")
    }
)

val page2: HTMLElement = createPage(
    h1Text = "You are in the page 2",
    pText = "Click here to go to page 3",
    action = {
        router.push(path = "/page3")
    }
)

val page3: HTMLElement = createPage(
    h1Text = "You are in the page 3",
    pText = "Click here to go to page 1",
    action = {
        router.replaceAll(path = "/page1")
    }
)

fun createPage(
    h1Text: String,
    pText: String,
    action: () -> Unit,
): HTMLElement = document.create.div {
    h1 {
        +h1Text
    }
    p {
        +pText
        onClickFunction = {
            action.invoke()
        }
    }
    p {
        +"Click here to pop"
        onClickFunction = {
            router.pop()
        }
    }
}