package dev.programadorthi.routing.statuspages

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.redirectToPath
import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.errors.RouteNotFoundException
import dev.programadorthi.routing.core.handle
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import io.ktor.http.Parameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class StatusPagesTest {
    @Test
    fun shouldHandleAnyException() =
        runTest {
            // GIVEN
            val job = Job()
            var result: Throwable? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(StatusPages) {
                        exception<Throwable> { _, cause ->
                            result = cause
                            job.complete()
                        }
                    }

                    handle(path = "/exception") {
                        throw IllegalArgumentException("stop routing")
                    }
                }

            // WHEN
            routing.call(uri = "/exception")
            advanceTimeBy(99)

            // THEN
            assertIs<IllegalArgumentException>(result)
            assertEquals("stop routing", result?.message)
        }

    @Test
    fun shouldHandleRouteNotFoundException() =
        runTest {
            // GIVEN
            val job = Job()
            var result: Throwable? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(StatusPages) {
                        exception<RouteNotFoundException> { _, cause ->
                            result = cause
                            job.complete()
                        }
                    }

                    handle(path = "/exception") {
                        throw IllegalArgumentException("stop routing")
                    }
                }

            // WHEN
            routing.call(uri = "/not-registered-path")
            advanceTimeBy(99)

            // THEN
            assertIs<RouteNotFoundException>(result)
            assertEquals("No matched subtrees found for: /not-registered-path", result?.message)
        }

    @Test
    fun shouldFromAnExceptionRedirectToAnother() =
        runTest {
            // GIVEN
            val job = Job()
            var result: Throwable? = null
            var aCall: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(StatusPages) {
                        exception<RouteNotFoundException> { call, cause ->
                            result = cause
                            call.redirectToPath(path = "/redirected")
                        }
                    }

                    handle(path = "/exception") {
                        throw IllegalArgumentException("stop routing")
                    }

                    handle(path = "/redirected") {
                        aCall = call
                        job.complete()
                    }
                }

            // WHEN
            routing.call(uri = "/not-registered-path")
            advanceTimeBy(99)

            // THEN
            assertIs<RouteNotFoundException>(result)
            assertEquals("No matched subtrees found for: /not-registered-path", result?.message)
            assertEquals("/redirected", "${aCall?.uri}")
            assertEquals("", "${aCall?.name}")
            assertEquals(RouteMethod.Empty, aCall?.routeMethod)
            assertEquals(Parameters.Empty, aCall?.parameters)
        }
}
