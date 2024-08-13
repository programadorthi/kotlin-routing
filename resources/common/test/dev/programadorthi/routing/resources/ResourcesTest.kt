package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.createApplicationPlugin
import dev.programadorthi.routing.core.application.hooks.CallFailed
import dev.programadorthi.routing.core.application.receive
import dev.programadorthi.routing.core.application.receiveNullable
import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.errors.RouteNotFoundException
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.resources.helper.ParentRouting
import dev.programadorthi.routing.resources.helper.Path
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ResourcesTest {
    @Test
    fun shouldExecuteByType() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var path: Path? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path> {
                        result = call
                        path = it
                        job.complete()
                    }
                }

            // WHEN
            routing.call(Path())
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
    fun shouldExecuteByTypeWithParameters() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var id: Path.Id? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path.Id> {
                        result = call
                        id = it
                        job.complete()
                    }
                }

            // WHEN
            routing.call(Path.Id(id = 123))
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
    fun shouldRedirectToOtherResource() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var id: Path.Id? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            routing.call(Path())
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
    fun shouldNavigateWhenSettingACustomRootPath() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var id: Path.Id? = null

            val routing =
                routing(
                    rootPath = "/resources",
                    parentCoroutineContext = coroutineContext + job,
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
            routing.call(Path())
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
    fun shouldHandleParentEventWhenCallingFromChild() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var id: Path.Id? = null

            val parent =
                routing(
                    rootPath = "/parent",
                    parentCoroutineContext = coroutineContext + job,
                ) {
                    install(Resources)

                    handle<Path.Id> {
                        result = call
                        id = it
                        job.complete()
                    }
                }

            val routing =
                routing(
                    rootPath = "/resources",
                    parentCoroutineContext = coroutineContext + job,
                    parent = parent,
                ) {
                    install(Resources)

                    handle<Path> {
                    }
                }

            // WHEN
            routing.call(Path.Id(id = 456))
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
    fun shouldHandleChildEventWhenCallingFromParent() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val parent =
                routing(
                    rootPath = "/parent",
                    parentCoroutineContext = coroutineContext + job,
                ) {
                    install(Resources)

                    handle<Path.Id> {
                    }
                }

            routing(
                rootPath = "/resources",
                parentCoroutineContext = coroutineContext + job,
                parent = parent,
            ) {
                install(Resources)

                handle<Path> {
                    result = call
                    job.complete()
                }
            }

            // WHEN
            parent.call(uri = "/resources/path")
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals("/resources/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Empty, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldUnregisterByType() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var exception: Throwable? = null

            val statusPages =
                createApplicationPlugin("status-pages") {
                    on(CallFailed) { call, cause ->
                        result = call
                        exception = cause
                        job.complete()
                    }
                }

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)
                    install(statusPages)

                    handle<Path.Id> {
                        error("No reached code")
                    }
                }

            // WHEN
            routing.unregisterResource<Path.Id>()
            routing.call(Path.Id(id = 123))
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertNotNull(exception)
            assertEquals("/path/123", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Empty, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
            assertIs<RouteNotFoundException>(exception)
            assertEquals(
                "No matched subtrees found for: /path/123",
                exception?.message,
            )
        }

    @Test
    fun shouldReturnsTrueWhenCanHandleAResource() {
        // GIVEN
        val routing =
            routing {
                install(Resources)

                handle<Path.Id> { }
            }

        // WHEN
        val result = routing.canHandleByResource<Path.Id>()

        // THEN
        assertTrue(result, "having a resource should can handle it")
    }

    @Test
    fun shouldReturnsFalseWhenCanNotHandleAResource() {
        // GIVEN
        val routing =
            routing {
                install(Resources)

                handle<Path> { }
            }

        // WHEN
        val result = routing.canHandleByResource<Path.Id>()

        // THEN
        assertFalse(result, "not having a resource should can't handle it")
    }

    @Test
    fun shouldReturnsTrueWhenCanHandleAResourceByMethod() {
        // GIVEN
        val method = RouteMethod("custom")
        val routing =
            routing {
                install(Resources)

                handle<Path.Id>(method = method) { }
            }

        // WHEN
        val result = routing.canHandleByResource<Path.Id>(method = method)

        // THEN
        assertTrue(result, "having a resource and method should can handle it")
    }

    @Test
    fun shouldReturnsFalseWhenCanNotHandleAResourceAndMethod() {
        // GIVEN
        val routing =
            routing {
                install(Resources)

                handle<Path> { }
            }

        // WHEN
        val result = routing.canHandleByResource<Path.Id>(method = RouteMethod("custom"))

        // THEN
        assertFalse(result, "not having a resource and method should can't handle it")
    }

    @Test
    fun shouldRedirectToOtherResourceAndMethod() =
        runTest {
            // GIVEN
            val job = Job()
            val method = RouteMethod("custom")
            var result: ApplicationCall? = null
            var id: Path.Id? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path> {
                        call.redirectTo(resource = Path.Id(id = 456), method = method)
                    }

                    handle<Path.Id>(method = method) {
                        result = call
                        id = it
                        job.complete()
                    }
                }

            // WHEN
            routing.call(Path())
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals(456, id?.id)
            assertEquals("/path/456", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(method, result?.routeMethod)
            assertEquals(parametersOf("id", "456"), result?.parameters)
        }

    @Test
    fun shouldGetNullBodyWhenNothingIsSent() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var body: String? = ""

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path> {
                        body = call.receiveNullable()
                        result = call
                    }
                }

            // WHEN
            routing.call(Path())
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals("/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Empty, result?.routeMethod)
            assertEquals(parametersOf(), result?.parameters)
            assertEquals(null, body, "body should be null")
        }

    @Test
    fun shouldGetNotNullBodyWhenSentSomething() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null
            var body: String? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(Resources)

                    handle<Path> {
                        body = call.receive()
                        result = call
                    }
                }

            // WHEN
            routing.callWithBody(Path(), "body content")
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals("/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Empty, result?.routeMethod)
            assertEquals(parametersOf(), result?.parameters)
            assertEquals("body content", body, "should have received a body")
        }

    @Test
    fun shouldDoParentToChildRoutingUsingChildType() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val parent =
                routing(
                    rootPath = "/parent",
                    parentCoroutineContext = coroutineContext + job,
                ) {
                    install(Resources)

                    handle<Path.Id> {
                    }
                }

            routing(
                rootPath = "/child",
                parentCoroutineContext = coroutineContext + job,
                parent = parent,
            ) {
                install(Resources)

                handle<ParentRouting.ChildRouting.Destination> {
                    result = call
                    job.complete()
                }
            }

            // WHEN
            parent.call(resource = ParentRouting.ChildRouting.Destination())
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals("/parent/child/destination", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Empty, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }
}
