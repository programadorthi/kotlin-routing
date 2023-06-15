package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.createApplicationPlugin
import dev.programadorthi.routing.core.application.hooks.CallFailed
import dev.programadorthi.routing.core.application.install
import dev.programadorthi.routing.core.application.redirectToName
import dev.programadorthi.routing.core.application.redirectToPath
import dev.programadorthi.routing.core.errors.RouteNotFoundException
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.util.Attributes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
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

    @Test
    fun shouldFailWhenCreatingChildRoutingWithSlashRootPath() = runTest {
        // WHEN
        val parent = routing(rootPath = "/initial") {}

        val result = assertFails {
            routing(rootPath = "/", parent = parent) {}
        }

        // THEN
        assertIs<IllegalStateException>(result)
        assertEquals("Child routing cannot have root path with '/' only. Please, provide a path to your child routing", result.message)
    }

    @Test
    fun shouldChangeRootPathWhenDeclaringAnInitialPath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(
            rootPath = "/initial",
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/default") {
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
                uri = "/initial/default"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/initial/default", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldNestedRoutingHaveYourOwnRouting() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
            rootPath = "/initial",
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/default") {
                handle { }
            }
        }

        val routing = routing(
            rootPath = "/middle",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/inner") {
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
                uri = "/middle/inner"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/middle/inner", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldNestedRoutingLookForParentRouteWhenNotFound() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
            rootPath = "/initial",
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/default") {
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        val routing = routing(
            rootPath = "/middle",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/inner") {
                handle { }
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/initial/default"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/initial/default", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldNestedRoutingHavingParentPathLookFromParentUntilNested() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
            rootPath = "/initial",
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/default") {
                handle { }
            }
        }

        val routing = routing(
            rootPath = "/middle",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/inner") {
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
                uri = "/initial/middle/inner"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/initial/middle/inner", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldFromParentRoutingNavigateDirectlyToNestedRoutingRoute() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(
            rootPath = "/initial",
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/default") {
                handle { }
            }
        }

        routing(
            rootPath = "/middle",
            parent = routing,
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/inner") {
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
                uri = "/initial/middle/inner"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/initial/middle/inner", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldSupportDeepNestedRouting() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val firstRouting = routing(
            rootPath = "/initial",
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/default") {
            }
        }

        val secondRouting = routing(
            rootPath = "/middle",
            parent = firstRouting,
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/inner") {
            }
        }

        val routing = routing(
            rootPath = "/end",
            parent = secondRouting,
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/content") {
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
                uri = "/end/content"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/end/content", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldSupportDeepNestedRoutingAndLookFromParentForARoute() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val firstRouting = routing(
            rootPath = "/initial",
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/default") {
            }
        }

        val secondRouting = routing(
            rootPath = "/middle",
            parent = firstRouting,
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/inner") {
            }
        }

        val routing = routing(
            rootPath = "/end",
            parent = secondRouting,
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/content") {
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
                uri = "/initial/middle/end/content"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/initial/middle/end/content", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldDisposeARoutingCleanAllRoutes() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var exception: Throwable? = null

        val statusPages = createApplicationPlugin("status-pages") {
            on(CallFailed) { call, cause ->
                result = call
                exception = cause
                job.complete()
            }
        }

        val firstRouting = routing(
            rootPath = "/initial",
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/default") {
            }
        }

        val secondRouting = routing(
            rootPath = "/middle",
            parent = firstRouting,
            parentCoroutineContext = coroutineContext + job
        ) {
            // Installing here because next routing will be disposed
            install(statusPages)

            route(path = "/inner") {
            }
        }

        val routing = routing(
            rootPath = "/end",
            parent = secondRouting,
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/content") {
                handle {
                }
            }
        }

        // WHEN
        routing.dispose()
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/initial/middle/end/content"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertNotNull(exception)
        assertEquals("/initial/middle/end/content", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
        assertIs<RouteNotFoundException>(exception)
        assertEquals("No matched subtrees found", exception?.message)
    }
}
