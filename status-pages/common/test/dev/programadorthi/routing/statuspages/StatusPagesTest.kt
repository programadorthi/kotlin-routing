package dev.programadorthi.routing.statuspages

import dev.programadorthi.routing.core.errors.RouteNotFoundException
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.routing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class StatusPagesTest {

    @Test
    fun shouldHandleAnyException() {
        var result: Throwable? = null

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                install(StatusPages) {
                    exception<Throwable> { _, cause ->
                        result = cause
                        handled()
                    }
                }

                push(path = "/exception") {
                    throw IllegalArgumentException("stop navigation")
                }
            }

            routing.push(path = "/exception")
        }

        assertIs<IllegalArgumentException>(result)

        val cast = result as IllegalArgumentException
        assertEquals(cast.message, "stop navigation")
    }

    @Test
    fun shouldHandleRouteNotFoundException() {
        var result: Throwable? = null

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                install(StatusPages) {
                    exception<RouteNotFoundException> { _, cause ->
                        result = cause
                        handled()
                    }
                }
            }

            routing.push(path = "/invalid-path")
        }

        assertIs<RouteNotFoundException>(result)

        val cast = result as RouteNotFoundException
        assertEquals(cast.message, "No matched subtrees found")
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
