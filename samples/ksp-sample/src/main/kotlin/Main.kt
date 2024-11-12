import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.generated.configure
import kotlinx.coroutines.delay


suspend fun main() {
    val router = routing {
        configure()
    }
    router.call(uri = "/path")
    delay(500)
    router.call(uri = "/path/1.2/routing")
    delay(500)
}
