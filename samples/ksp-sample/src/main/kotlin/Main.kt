import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.callWithBody
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.generated.configure
import dev.programadorthi.routing.sample.User
import io.ktor.http.parametersOf
import kotlin.random.Random
import kotlinx.coroutines.delay

suspend fun main() {
    val router = routing {
        configure()
    }
    router.call(uri = "/path")
    delay(500)
    router.call(uri = "/path/1.2")
    delay(500)
    router.call(name = "named", parameters = parametersOf("name", "Routing"))
    delay(500)
    //router.call(name = "custom", parameters = parametersOf("random", "abc123"))
    router.call(uri = "/custom/${Random.Default.nextInt()}")
    delay(500)
    router.call(uri = "/optional")
    delay(500)
    router.call(uri = "/optional/ABC")
    delay(500)
    router.call(uri = "/tailcard/p1")
    delay(500)
    router.call(uri = "/tailcard/p1/p2/p3/p4")
    delay(500)
    router.call(uri = "/foo/hello") // regex1
    delay(500)
    router.call(uri = "/456") // regex2
    delay(500)
    router.callWithBody(uri = "/with-body", body = User(id = 456, name = "With Body"))
    delay(500)
    router.call(uri = "/with-null-body")
    delay(500)
    router.callWithBody(uri = "/with-null-body", body = User(id = 789, name = "No null Body"))
    delay(500)
}
