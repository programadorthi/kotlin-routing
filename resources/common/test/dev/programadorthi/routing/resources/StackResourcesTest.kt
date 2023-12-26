package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.StackRouting
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.isPop
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.routing
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.resources.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class StackResourcesTest {

    @Resource("/path")
    class Path {
        @Resource("{id}")
        class Id(val parent: Path = Path(), val id: Int)
    }

    @Test
    fun shouldPushByType() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var path: Path? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(StackRouting)

            handle<Path> {
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
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldReplaceByType() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var path: Path? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(StackRouting)

            handle<Path> {
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
        assertEquals(StackRouteMethod.Replace, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldReplaceAllByType() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var path: Path? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(StackRouting)

            handle<Path> {
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
        assertEquals(StackRouteMethod.ReplaceAll, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldPushByTypeAndParameters() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var id: Path.Id? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(StackRouting)

            handle<Path.Id> {
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
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(parametersOf("id", "123"), result?.parameters)
    }

    @Test
    fun shouldReplaceByTypeAndParameters() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var id: Path.Id? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(StackRouting)

            handle<Path.Id> {
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
        assertEquals(StackRouteMethod.Replace, result?.routeMethod)
        assertEquals(parametersOf("id", "123"), result?.parameters)
    }

    @Test
    fun shouldReplaceAllByTypeAndParameters() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var id: Path.Id? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(StackRouting)

            handle<Path.Id> {
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
        assertEquals(StackRouteMethod.ReplaceAll, result?.routeMethod)
        assertEquals(parametersOf("id", "123"), result?.parameters)
    }

    @Test
    fun shouldPopAPushedType() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        val ids = mutableListOf<Path.Id>()

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(StackRouting)

            handle<Path.Id> {
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
        routing.pop()
        advanceTimeBy(99)
        routing.pop()
        advanceTimeBy(99)
        routing.pop()
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path/1", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Pop, result?.routeMethod)
        assertEquals(parametersOf("id", "1"), result?.parameters)
        // 1, 2, 3 are pushed
        // 3, 2 are the first pop (notify 3 for pop and 2 for it route method)
        // 2, 1 are the second pop (notify 2 for pop and 1 for it route method)
        // 1 is the last pop (notify 1 for pop and there is no more items on the stack)
        assertEquals(listOf(1, 2, 3, 3, 2, 2, 1, 1), ids.map { it.id })
    }

    @Test
    fun shouldHandleAnyStackAction() = runTest {
        // GIVEN
        val job = Job()
        val result = mutableListOf<Pair<ApplicationCall, Path.Id>>()

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(StackRouting)

            handle<Path.Id> { id ->
                result += call to id
                if (call.isPop()) {
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
        routing.pop()
        advanceTimeBy(99)

        // THEN
        assertEquals(StackRouteMethod.Push, result[0].first.routeMethod)
        assertEquals(1, result[0].second.id)

        assertEquals(StackRouteMethod.Push, result[1].first.routeMethod)
        assertEquals(2, result[1].second.id)

        assertEquals(StackRouteMethod.Replace, result[2].first.routeMethod)
        assertEquals(3, result[2].second.id)

        assertEquals(StackRouteMethod.Pop, result[3].first.routeMethod)
        assertEquals(2, result[3].second.id)

        assertEquals(StackRouteMethod.ReplaceAll, result[4].first.routeMethod)
        assertEquals(4, result[4].second.id)

        assertEquals(StackRouteMethod.Pop, result[5].first.routeMethod)
        assertEquals(3, result[5].second.id)

        assertEquals(StackRouteMethod.Pop, result[6].first.routeMethod)
        assertEquals(1, result[6].second.id)

        assertEquals(StackRouteMethod.Pop, result[7].first.routeMethod)
        assertEquals(4, result[7].second.id)
    }
}
