package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import io.ktor.resources.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ResourcesTest {

    @Resource("/routing")
    class Routing {
        @Resource("{id}")
        class Id(val parent: Routing = Routing(), val id: Int)
    }

    @Test
    fun shouldPushByType() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                install(Resources)

                push<Routing> {
                    result = "path-handled"
                    handled()
                }
            }

            routing.push(Routing())
        }

        assertEquals(result, "path-handled")
    }

    @Test
    fun shouldReplaceByType() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                install(Resources)

                replace<Routing> {
                    result = "path-handled"
                    handled()
                }
            }

            routing.replace(Routing())
        }

        assertEquals(result, "path-handled")
    }

    @Test
    fun shouldReplaceAllByType() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                install(Resources)

                replace<Routing> {
                    result = "path-handled"
                    handled()
                }
            }

            routing.replaceAll(Routing())
        }

        assertEquals(result, "path-handled")
    }

    @Test
    fun shouldNavigateByTypeWithParameters() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                install(Resources)

                push<Routing.Id> { id ->
                    result = "${id.id}"
                    handled()
                }
            }

            routing.push(Routing.Id(id = 123))
        }

        assertEquals(result, "123")
    }

    private fun whenBody(
        body: CoroutineContext.(() -> Unit) -> Unit
    ) = runTest {
        val job = Job()
        (coroutineContext + job).body {
            job.cancel()
        }
        job.join()
    }
}
