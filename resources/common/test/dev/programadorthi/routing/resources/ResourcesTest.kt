package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.install
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
class ResourcesTest {

    @Resource("/path")
    class Path {
        @Resource("{id}")
        class Id(val parent: Path = Path(), val id: Int)
    }

    @Test
    fun shouldExecuteByType() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var path: Path? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)

            handle<Path> {
                result = call
                path = it
                job.complete()
            }
        }

        // WHEN
        routing.execute(Path())
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertNotNull(path)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldExecuteByTypeWithParameters() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var id: Path.Id? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)

            handle<Path.Id> {
                result = call
                id = it
                job.complete()
            }
        }

        // WHEN
        routing.execute(Path.Id(id = 123))
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals(123, id?.id)
        assertEquals("/path/123", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(parametersOf("id", "123"), result?.parameters)
    }

    @Test
    fun shouldRedirectToOtherResource() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var id: Path.Id? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)

            handle<Path> {
                call.redirectTo(resource = Path.Id(id = 456))
            }

            handle<Path.Id> {
                result = call
                id = it
                job.complete()
            }
        }

        // WHEN
        routing.execute(Path())
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals(456, id?.id)
        assertEquals("/path/456", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(parametersOf("id", "456"), result?.parameters)
    }

    @Test
    fun shouldNavigateWhenSettingACustomRootPath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var id: Path.Id? = null

        val routing = routing(
            rootPath = "/resources",
            parentCoroutineContext = coroutineContext + job
        ) {
            install(Resources)

            handle<Path> {
                call.redirectTo(resource = Path.Id(id = 456))
            }

            handle<Path.Id> {
                result = call
                id = it
                job.complete()
            }
        }

        // WHEN
        routing.execute(Path())
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals(456, id?.id)
        assertEquals("/path/456", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(parametersOf("id", "456"), result?.parameters)
    }

    @Test
    fun shouldHandleParentEventWhenCallingFromChild() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var id: Path.Id? = null

        val parent = routing(
            rootPath = "/parent",
            parentCoroutineContext = coroutineContext + job
        ) {
            install(Resources)

            handle<Path.Id> {
                result = call
                id = it
                job.complete()
            }
        }

        val routing = routing(
            rootPath = "/resources",
            parentCoroutineContext = coroutineContext + job,
            parent = parent
        ) {
            install(Resources)

            handle<Path> {
            }
        }

        // WHEN
        routing.execute(Path.Id(id = 456))
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals(456, id?.id)
        assertEquals("/path/456", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(parametersOf("id", "456"), result?.parameters)
    }

    @Test
    fun shouldHandleChildEventWhenCallingFromParent() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
            rootPath = "/parent",
            parentCoroutineContext = coroutineContext + job
        ) {
            install(Resources)

            handle<Path.Id> {
            }
        }

        routing(
            rootPath = "/resources",
            parentCoroutineContext = coroutineContext + job,
            parent = parent
        ) {
            install(Resources)

            handle<Path> {
                result = call
                job.complete()
            }
        }

        // WHEN
        // TODO: For now, parent looking for child is not supported using the type. We need to use path based
        parent.call(uri = "/resources/path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/resources/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }
}
