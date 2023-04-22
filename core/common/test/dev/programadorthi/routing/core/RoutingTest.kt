package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.redirectToName
import dev.programadorthi.routing.core.application.redirectToPath
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.util.Attributes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class RoutingTest {

    class BasicApplicationCall(
        override val application: Application,
        override val name: String = "",
        override val uri: String = "",
        override val parameters: Parameters = Parameters.Empty,
    ) : ApplicationCall {
        override val attributes: Attributes = Attributes()

        override val routeMethod: RouteMethod get() = RouteMethod.Empty
    }

    @Test
    fun shouldCreateARouteAndHandleIt() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            route(path = "/path") {
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/path",
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldCreateARouteUsingHandleDirectly() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            handle(path = "/path") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/path",
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldPassParametersInRoutingUsingPath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            handle(path = "/path") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/path",
                parameters = parametersOf("key", "values"),
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(parametersOf("key", "values"), result?.parameters)
    }

    @Test
    fun shouldRoutingUsingName() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            handle(path = "/path", name = "named") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                name = "named",
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("named", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldPassParametersInRoutingUsingName() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            handle(path = "/path", name = "named") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                name = "named",
                parameters = parametersOf("key", "values"),
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("named", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(parametersOf("key", "values"), result?.parameters)
    }

    @Test
    fun shouldGetParametersFromPath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            handle(path = "/path/{id}") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/path/123"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path/123", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(parametersOf("id", "123"), result?.parameters)
    }

    @Test
    fun shouldReplacePathParametersWhenProvidingParametersToNamedRouting() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            handle(path = "/path/{id}/hello/{name}", name = "named") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                name = "named",
                parameters = parametersOf(
                    "id" to listOf("123"),
                    "name" to listOf("routing"),
                )
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path/123/hello/routing", "${result?.uri}")
        assertEquals("named", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(
            parametersOf(
                "id" to listOf("123"),
                "name" to listOf("routing"),
            ),
            result?.parameters
        )
    }

    @Test
    fun shouldCaptureQueryParametersWhenProvided() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            handle(path = "/path") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/path?q=routing&kind=multiplatform"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path?q=routing&kind=multiplatform", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(
            parametersOf(
                "q" to listOf("routing"),
                "kind" to listOf("multiplatform"),
            ),
            result?.parameters
        )
    }

    @Test
    fun shouldRedirectToAnotherPath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            route(path = "/path1") {
                handle {
                    call.redirectToPath(path = "/path2")
                }
            }
            handle(path = "/path2") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/path1"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path2", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldRedirectToAnotherName() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            route(path = "/path1", name = "path1") {
                handle {
                    call.redirectToName(name = "path2")
                }
            }
            handle(path = "/path2", name = "path2") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                name = "path1"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path2", "${result?.uri}")
        assertEquals("path2", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldPutParametersWhenRedirectToAnotherName() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            route(path = "/path1", name = "path1") {
                handle {
                    call.redirectToName(name = "path2", parameters = parametersOf("key", "value"))
                }
            }
            handle(path = "/path2", name = "path2") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                name = "path1"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path2", "${result?.uri}")
        assertEquals("path2", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(parametersOf("key", "value"), result?.parameters)
    }

    @Test
    fun shouldReplaceParametersWhenRedirectToAnotherName() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            route(path = "/path1", name = "path1") {
                handle {
                    call.redirectToName(
                        name = "path2",
                        parameters = parametersOf(
                            "id" to listOf("123"),
                            "key" to listOf("value"),
                        )
                    )
                }
            }
            handle(path = "/path2/{id}", name = "path2") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                name = "path1"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path2/123", "${result?.uri}")
        assertEquals("path2", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(
            parametersOf(
                "id" to listOf("123"),
                "key" to listOf("value"),
            ),
            result?.parameters
        )
    }

    @Test
    fun shouldRoutingByRegex() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            route(path = Regex("/(?<number>\\d+)")) {
                handle(path = "/hello") {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/123/hello"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/123/hello", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(parametersOf("number", "123"), result?.parameters)
    }

    @Test
    fun shouldGetParametersWhenRoutingByRegex() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            route(path = Regex("/(?<number>\\d+)")) {
                handle(path = Regex("(?<user>\\w+)/(?<login>.+)")) {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/456/qwe/rty"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/456/qwe/rty", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(
            parametersOf(
                "number" to listOf("456"),
                "user" to listOf("qwe"),
                "login" to listOf("rty"),
            ),
            result?.parameters
        )
    }

    @Test
    fun shouldInstallAndRoutingOnDemand() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            route(path = "/default") {
                handle { }
            }
        }

        routing.handle(path = "/ondemand") {
            result = call
            job.complete()
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/ondemand"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/ondemand", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }
}
