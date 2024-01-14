package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.resources.helper.Path
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class StackResourcesTest {
    @Test
    fun shouldPushByType() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var path: Path? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path>(method = RouteMethod.Push) {
                        result = call
                        path = it
                        job.complete()
                    }
                }

            // WHEN
            routing.push(Path())
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertNotNull(path)
            assertEquals("/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldReplaceByType() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var path: Path? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path>(method = RouteMethod.Replace) {
                        result = call
                        path = it
                        job.complete()
                    }
                }

            // WHEN
            routing.replace(Path())
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertNotNull(path)
            assertEquals("/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Replace, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldReplaceAllByType() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var path: Path? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path>(method = RouteMethod.ReplaceAll) {
                        result = call
                        path = it
                        job.complete()
                    }
                }

            // WHEN
            routing.replaceAll(Path())
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertNotNull(path)
            assertEquals("/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.ReplaceAll, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldPushByTypeAndParameters() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var id: Path.Id? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path.Id>(method = RouteMethod.Push) {
                        result = call
                        id = it
                        job.complete()
                    }
                }

            // WHEN
            routing.push(Path.Id(id = 123))
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals(123, id?.id)
            assertEquals("/path/123", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf("id", "123"), result?.parameters)
        }

    @Test
    fun shouldReplaceByTypeAndParameters() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var id: Path.Id? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path.Id>(method = RouteMethod.Replace) {
                        result = call
                        id = it
                        job.complete()
                    }
                }

            // WHEN
            routing.replace(Path.Id(id = 123))
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals(123, id?.id)
            assertEquals("/path/123", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Replace, result?.routeMethod)
            assertEquals(parametersOf("id", "123"), result?.parameters)
        }

    @Test
    fun shouldReplaceAllByTypeAndParameters() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var id: Path.Id? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path.Id>(method = RouteMethod.ReplaceAll) {
                        result = call
                        id = it
                        job.complete()
                    }
                }

            // WHEN
            routing.replaceAll(Path.Id(id = 123))
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals(123, id?.id)
            assertEquals("/path/123", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.ReplaceAll, result?.routeMethod)
            assertEquals(parametersOf("id", "123"), result?.parameters)
        }

    @Test
    fun shouldPushedTypeMultipleTimes() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            val ids = mutableListOf<Path.Id>()

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path.Id>(method = RouteMethod.Push) {
                        result = call
                        ids += it
                    }
                }

            // WHEN
            routing.push(Path.Id(id = 1))
            advanceTimeBy(99)
            routing.push(Path.Id(id = 2))
            advanceTimeBy(99)
            routing.push(Path.Id(id = 3))
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals("/path/3", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf("id", "3"), result?.parameters)
            assertEquals(listOf(1, 2, 3), ids.map { it.id })
        }

    @Test
    fun shouldHandleAnyStackAction() =
        runTest {
            // GIVEN
            val job = Job()
            val result = mutableListOf<Pair<ApplicationCall, Path.Id>>()

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path.Id> { id ->
                        result += call to id
                        if (id.id > 3) {
                            job.complete()
                        }
                    }
                }

            // WHEN
            routing.push(Path.Id(id = 1))
            advanceTimeBy(99)
            routing.push(Path.Id(id = 2))
            advanceTimeBy(99)
            routing.replace(Path.Id(id = 3))
            advanceTimeBy(99)
            routing.replaceAll(Path.Id(id = 4))
            advanceTimeBy(99)

            // THEN
            assertEquals(RouteMethod.Push, result[0].first.routeMethod)
            assertEquals(1, result[0].second.id)

            assertEquals(RouteMethod.Push, result[1].first.routeMethod)
            assertEquals(2, result[1].second.id)

            assertEquals(RouteMethod.Replace, result[2].first.routeMethod)
            assertEquals(3, result[2].second.id)

            assertEquals(RouteMethod.ReplaceAll, result[3].first.routeMethod)
            assertEquals(4, result[3].second.id)
        }
}
