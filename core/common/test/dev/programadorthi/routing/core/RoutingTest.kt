package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.createApplicationPlugin
import dev.programadorthi.routing.core.application.hooks.CallFailed
import dev.programadorthi.routing.core.application.redirectToName
import dev.programadorthi.routing.core.application.redirectToPath
import dev.programadorthi.routing.core.errors.RouteNotFoundException
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
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
        routing.call(uri = "/path")
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
        routing.call(uri = "/path")
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
        routing.call(
            uri = "/path",
            parameters = parametersOf("key", "values"),
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
        routing.call(name = "named")
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
        routing.call(
            name = "named",
            parameters = parametersOf("key", "values"),
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
        routing.call(uri = "/path/123")
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
        routing.call(
            name = "named",
            parameters = parametersOf(
                "id" to listOf("123"),
                "name" to listOf("routing"),
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
        routing.call(uri = "/path?q=routing&kind=multiplatform")
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
        routing.call(uri = "/path1")
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
        routing.call(name = "path1")
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
        routing.call(name = "path1")
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
        routing.call(name = "path1")
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
        routing.call(uri = "/123/hello")
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
        routing.call(uri = "/456/qwe/rty")
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
        routing.call(uri = "/ondemand")
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
        assertEquals(
            "Child routing cannot have root path with '/' only. Please, provide a path to your child routing",
            result.message
        )
    }

    @Test
    fun shouldChangeRootPathWhenDeclaringARootPath() = runTest {
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
        routing.call(uri = "/initial/default")
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
        routing.call(uri = "/middle/inner")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/middle/inner", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldNestedRoutingHaveYourOwnRoutingWhenParentHasDefaultRootPath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
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
        routing.call(uri = "/middle/inner")
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
        routing.call(uri = "/initial/default")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/initial/default", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldNestedRoutingLookForParentRouteWhenNotFoundAndParentHasDefaultRootPath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
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
        routing.call(uri = "/default")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/default", "${result?.uri}")
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
        routing.call(uri = "/initial/middle/inner")
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
        routing.call(uri = "/initial/middle/inner")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/initial/middle/inner", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldFromParentRoutingWithDefaultRootPathNavigateDirectlyToNestedRoutingRoute() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(
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
        routing.call(uri = "/middle/inner")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/middle/inner", "${result?.uri}")
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
        routing.call(uri = "/end/content")
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
        routing.call(uri = "/initial/middle/end/content")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/initial/middle/end/content", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldSupportDeepNestedRoutingAndLookFromParentWithDefaultRootPathForARoute() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val firstRouting = routing(
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
        routing.call(uri = "/middle/end/content")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/middle/end/content", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldDisposeARoutingCleanItChildrenRoutes() = runTest {
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
            install(statusPages)

            route(path = "/default") {
            }
        }

        val secondRouting = routing(
            rootPath = "/middle",
            parent = firstRouting,
            parentCoroutineContext = coroutineContext + job
        ) {
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
        routing.call(uri = "/initial/middle/end/content")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertNotNull(exception)
        assertEquals("/initial/middle/end/content", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
        assertIs<RouteNotFoundException>(exception)
        assertEquals(
            "No matched subtrees found for: /initial/middle/end/content",
            exception?.message
        )
    }

    @Test
    fun shouldRoutingWhenExecuteWithoutRootPathInTheURI() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(
            rootPath = "/initial",
            parentCoroutineContext = coroutineContext + job
        ) {
            handle(path = "/default") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.call(uri = "/default")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/default", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldRoutingWithDefaultRootPathWhenExecuteWithoutRootPathInTheURI() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(
            parentCoroutineContext = coroutineContext + job
        ) {
            handle(path = "/default") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.call(uri = "/default")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/default", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldRoutingWithParentWhenCallWithoutRootPathOnChild() = runTest {
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
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        val routing = routing(
            rootPath = "/end",
            parent = secondRouting,
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/content") {
            }
        }

        // WHEN
        routing.call(uri = "/inner")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/inner", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldConnectNestedRoutingWhenCreatingRouteOnDemand() = runTest {
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
        }

        val routing = routing(
            rootPath = "/end",
            parent = secondRouting,
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/content") {
            }
        }

        secondRouting.route(path = "/inner") {
            handle {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.dispose()
        firstRouting.call(uri = "/middle/inner")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/middle/inner", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldRecognizeChildRouteWhenItPathStartEqualsToParentRouting() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(
            rootPath = "/initial",
            parentCoroutineContext = coroutineContext + job
        ) {
            handle(path = "/initial-default") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.call(uri = "/initial-default")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/initial-default", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldSupportLargeRootPathWhenRootPathHasMoreSlashLevels() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(
            rootPath = "/initial/middle/end",
            parentCoroutineContext = coroutineContext + job
        ) {
            handle(path = "/end-default") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.call(uri = "/end-default")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/end-default", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldRecognizeChildRouteWhenHavingInnerRoutingAndItPathStartEqualsToParentRouting() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val parent = routing(
                rootPath = "/parent",
                parentCoroutineContext = coroutineContext + job
            ) {
                handle(path = "/parent-default") {}
            }

            val routing = routing(
                rootPath = "child",
                parent = parent,
                parentCoroutineContext = coroutineContext + job
            ) {
                handle(path = "/child-default") {
                    result = call
                    job.complete()
                }
            }

            // WHEN
            routing.call(uri = "/child-default")
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals("/child-default", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Empty, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldSupportLargeRootPathWhenHavingInnerRoutingAndRootPathHasMoreSlashLevels() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
            rootPath = "/initial/middle",
            parentCoroutineContext = coroutineContext + job
        ) {
            handle(path = "/initial") {
                result = call
                job.complete()
            }
        }

        val routing = routing(
            rootPath = "/end",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            handle(path = "/end-default") {}
        }

        // WHEN
        routing.call(uri = "/initial")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/initial", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldSupportRoutingWhenRootPathAndInnerRouteHaveSamePath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(
            rootPath = "/path",
            parentCoroutineContext = coroutineContext + job
        ) {
            handle(path = "/path") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.call(uri = "/path/path") // Having rootPath and a route with same value your must provide rootPath on the URI
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldSupportDeepRoutingWhenRootPathAndInnerRoutesHaveSamePath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(
            rootPath = "/path",
            parentCoroutineContext = coroutineContext + job
        ) {
            route(path = "/path") {
                route(path = "/path") {
                    handle(path = "/path") {
                        result = call
                        job.complete()
                    }
                }
            }
        }

        // WHEN
        routing.call(uri = "/path/path/path/path") // Having rootPath and a route with same value your must provide rootPath on the URI
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path/path/path/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }
}
