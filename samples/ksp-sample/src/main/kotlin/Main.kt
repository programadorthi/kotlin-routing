import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.generated.configure
import io.ktor.http.parametersOf
import kotlinx.coroutines.delay
import kotlin.random.Random


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
}
