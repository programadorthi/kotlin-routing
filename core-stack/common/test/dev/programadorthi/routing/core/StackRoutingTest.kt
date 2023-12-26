package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.redirectToName
import dev.programadorthi.routing.core.application.redirectToPath
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class StackRoutingTest {

    @BeforeTest
    fun beforeEachTest() {
        StackManager.stackManagerNotifier = null
    }

    @Test
    fun shouldFailWhenMissingStackPlugin() = runTest {
        // GIVEN
        val routing = routing {
            route(path = "/path") {}
        }

        // WHEN
        val result = assertFails {
            routing.push(path = "/path")
        }

        // THEN
        assertIs<IllegalStateException>(result)
        assertEquals("StackRouting plugin not installed", result.message)
    }

    @Test
    fun shouldPushAPath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = "/path") {
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.push(path = "/path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldPushAPathWithParameters() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = "/path") {
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.push(path = "/path", parameters = parametersOf("key", "value"))
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(parametersOf("key", "value"), result?.parameters)
    }

    @Test
    fun shouldPushAPathByName() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = "/path", name = "path") {
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.pushNamed(name = "path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("path", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldPushAPathByNameWithParameters() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = "/path", name = "path") {
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.pushNamed(name = "path", parameters = parametersOf("key", "value"))
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("path", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(parametersOf("key", "value"), result?.parameters)
    }

    @Test
    fun shouldPushAPathByNameWithPathParameters() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = "/path/{id}", name = "path") {
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.pushNamed(name = "path", parameters = parametersOf("id", "123"))
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path/123", "${result?.uri}")
        assertEquals("path", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(parametersOf("id", "123"), result?.parameters)
    }

    @Test
    fun shouldPopWithParameters() = runTest {
        // GIVEN
        val job = Job()
        val result = mutableListOf<ApplicationCall>()

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = "/path", name = "path") {
                handle {
                    result += call
                }
            }
        }

        // WHEN
        // A route must exist to pop
        routing.push(path = "/path")
        advanceTimeBy(99)
        routing.pop(parameters = parametersOf("key", "value"))
        advanceTimeBy(99)

        // THEN
        assertEquals(2, result.size)
        // First handle is a push call
        assertEquals("/path", result[0].uri)
        assertEquals("", result[0].name)
        assertEquals(StackRouteMethod.Push, result[0].routeMethod)
        assertEquals(Parameters.Empty, result[0].parameters)
        // Second handle is a pop call
        assertEquals("/path", result[1].uri)
        assertEquals("", result[1].name)
        assertEquals(StackRouteMethod.Pop, result[1].routeMethod)
        assertEquals(parametersOf("key", "value"), result[1].parameters)
    }

    @Test
    fun shouldPopNamedRoutes() = runTest {
        // GIVEN
        val job = Job()
        val result = mutableListOf<ApplicationCall>()

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = "/path", name = "path") {
                handle {
                    result += call
                }
            }
        }

        // WHEN
        // A route must exist to pop
        routing.pushNamed(name = "path")
        advanceTimeBy(99)
        routing.pop(parameters = parametersOf("key", "value"))
        advanceTimeBy(99)

        // THEN
        assertEquals(2, result.size)
        // First handle is a push call
        assertEquals("/path", result[0].uri)
        assertEquals("path", result[0].name)
        assertEquals(StackRouteMethod.Push, result[0].routeMethod)
        assertEquals(Parameters.Empty, result[0].parameters)
        // Second handle is a pop call
        assertEquals("/path", result[1].uri)
        assertEquals("path", result[1].name)
        assertEquals(StackRouteMethod.Pop, result[1].routeMethod)
        assertEquals(parametersOf("key", "value"), result[1].parameters)
    }

    @Test
    fun shouldCreateARouteUsingHandleDirectly() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            handle(path = "/path") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.push(path = "/path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldReplaceAPath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            handle(path = "/path") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.replace(path = "/path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Replace, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldReplaceAllPaths() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            handle(path = "/path") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.replaceAll(path = "/path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.ReplaceAll, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldRedirectToOtherRouteByPath() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = "/path") {
                handle {
                    if (!call.routeMethod.isStackPop()) {
                        call.redirectToPath(path = "/path2")
                    }
                }
            }

            route(path = "/path2") {
                // Redirect will match same routing call (push to push, replace to replace, etc)
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.replace(path = "/path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path2", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Replace, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldRedirectToOtherRouteByName() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = "/path", name = "path") {
                handle {
                    call.redirectToName(name = "path2")
                }
            }

            route(path = "/path2", name = "path2") {
                // Redirect will match same routing call (push to push, replace to replace, etc)
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.pushNamed(name = "path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path2", "${result?.uri}")
        assertEquals("path2", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldRouteByRegex() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = Regex("/(?<number>\\d+)")) {
                handle(path = "/regex") {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.push(path = "/123/regex")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/123/regex", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(parametersOf("number", "123"), result?.parameters)
    }

    @Test
    fun shouldNeglectARouteWhenFlagged() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            handle(path = "/path") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.push(path = "/path", neglect = true)
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(parametersOf(), result?.parameters)
    }

    @Test
    fun shouldNotNeglectARouteWhenNotFlagged() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            handle(path = "/path") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.push(path = "/path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(parametersOf(), result?.parameters)
    }

    @Test
    fun shouldPushParentPathWhenRoutingFromChild() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
            parentCoroutineContext = coroutineContext + job
        ) {
            install(StackRouting)

            route(path = "/pathParent") {
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        val routing = routing(
            rootPath = "/child",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            install(StackRouting)

            route(path = "/pathChild") {
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        routing.push(path = "/pathParent")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/pathParent", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldPushChildPathWhenRoutingFromParent() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
            parentCoroutineContext = coroutineContext + job
        ) {
            install(StackRouting)

            route(path = "/pathParent") {
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        routing(
            rootPath = "/child",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            install(StackRouting)

            route(path = "/pathChild") {
                handle {
                    result = call
                    job.complete()
                }
            }
        }

        // WHEN
        parent.push(path = "/child/pathChild")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/child/pathChild", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldPreviousCallBeNull() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = "/path", name = "path") {
                handle {
                    result = previousCall()
                }
            }
        }

        // WHEN
        routing.push(path = "/path")
        advanceTimeBy(99)

        // THEN
        assertNull(result)
    }

    @Test
    fun shouldPreviousCallNotBeNull() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            route(path = "/path", name = "path") {
                handle {}
            }

            route(path = "/path2", name = "path2") {
                handle {
                    result = previousCall()
                    job.complete()
                }
            }
        }

        // WHEN
        // A route must exist to pop
        routing.push(path = "/path")
        advanceTimeBy(99)
        routing.push(path = "/path2")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/path", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(parametersOf(), result?.parameters)
    }

    @Test
    fun shouldInstallStackManagerRegisterIt() = runTest {
        // GIVEN
        val stackManagerNotifier = FakeStackManagerNotifier()
        StackManager.stackManagerNotifier = stackManagerNotifier

        val routing = routing {
            // WHEN
            install(StackRouting)
        }

        // THEN
        val environment = routing.environment
        val providerId = "${environment?.parentRouting}#${environment?.rootPath}"
        assertEquals(providerId, stackManagerNotifier.subscriptions.keys.first())
    }

    @Test
    fun shouldDisposeRoutingUnRegisterStackManager() = runTest {
        // GIVEN
        val stackManagerNotifier = FakeStackManagerNotifier()
        StackManager.stackManagerNotifier = stackManagerNotifier

        val routing = routing {
            // WHEN
            install(StackRouting)
        }
        val registeredKey = stackManagerNotifier.subscriptions.keys.first()
        routing.dispose()
        advanceTimeBy(99)

        // THEN
        val environment = routing.environment
        val providerId = "${environment?.parentRouting}#${environment?.rootPath}"
        assertEquals(providerId, registeredKey)
        assertEquals(null, stackManagerNotifier.subscriptions.keys.firstOrNull())
    }

    @Test
    fun shouldHandleAnyStackAction() = runTest {
        // GIVEN
        val job = Job()
        val result = mutableListOf<ApplicationCall>()

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            handle(path = "/path", name = "path") {
                result += call
            }
        }

        // WHEN
        // A route must exist to pop
        routing.push(path = "/path")
        advanceTimeBy(99)
        routing.pushNamed(name = "path")
        advanceTimeBy(99)
        routing.replace(path = "/path")
        advanceTimeBy(99)
        routing.replaceNamed(name = "path")
        advanceTimeBy(99)
        routing.replaceAll(path = "/path")
        advanceTimeBy(99)
        routing.replaceAllNamed(name = "path")
        advanceTimeBy(99)
        routing.pop(parameters = parametersOf("key", "value"))
        advanceTimeBy(99)

        // THEN
        // First handle is a push call
        assertEquals(StackRouteMethod.Push, result[0].routeMethod)
        assertEquals("", result[0].name)
        assertEquals("/path", result[0].uri)
        assertEquals(Parameters.Empty, result[0].parameters)
        // Second handle is a push call
        assertEquals(StackRouteMethod.Push, result[1].routeMethod)
        assertEquals("path", result[1].name)
        assertEquals("/path", result[1].uri)
        assertEquals(Parameters.Empty, result[1].parameters)
        // Third handle is a replace call
        assertEquals(StackRouteMethod.Replace, result[2].routeMethod)
        assertEquals("", result[2].name)
        assertEquals("/path", result[2].uri)
        assertEquals(Parameters.Empty, result[2].parameters)
        // Fourth handle is a pop call emitted by replace
        assertEquals(StackRouteMethod.Pop, result[3].routeMethod)
        assertEquals("path", result[3].name)
        assertEquals("/path", result[3].uri)
        assertEquals(Parameters.Empty, result[3].parameters)
        // Fifth handle is a replace call
        assertEquals(StackRouteMethod.Replace, result[4].routeMethod)
        assertEquals("path", result[4].name)
        assertEquals("/path", result[4].uri)
        assertEquals(Parameters.Empty, result[4].parameters)
        // Sixth handle is a pop call emitted by replace
        assertEquals(StackRouteMethod.Pop, result[5].routeMethod)
        assertEquals("", result[5].name)
        assertEquals("/path", result[5].uri)
        assertEquals(Parameters.Empty, result[5].parameters)
        // Seventh handle is a replace all call
        assertEquals(StackRouteMethod.ReplaceAll, result[6].routeMethod)
        assertEquals("", result[6].name)
        assertEquals("/path", result[6].uri)
        assertEquals(Parameters.Empty, result[6].parameters)
        // Eighth handle is a pop call emitted by replace all
        assertEquals(StackRouteMethod.Pop, result[7].routeMethod)
        assertEquals("path", result[7].name)
        assertEquals("/path", result[7].uri)
        assertEquals(Parameters.Empty, result[7].parameters)
        // Ninth handle is a pop call emitted by replace all
        assertEquals(StackRouteMethod.Pop, result[8].routeMethod)
        assertEquals("", result[8].name)
        assertEquals("/path", result[8].uri)
        assertEquals(Parameters.Empty, result[8].parameters)
        // Tenth handle is a replace all call
        assertEquals(StackRouteMethod.ReplaceAll, result[9].routeMethod)
        assertEquals("path", result[9].name)
        assertEquals("/path", result[9].uri)
        assertEquals(Parameters.Empty, result[9].parameters)
        // Eleventh handle is a pop call emitted by replace all
        assertEquals(StackRouteMethod.Pop, result[10].routeMethod)
        assertEquals("", result[10].name)
        assertEquals("/path", result[10].uri)
        assertEquals(Parameters.Empty, result[10].parameters)
        // Twelfth handle is a pop call
        assertEquals(StackRouteMethod.Pop, result[11].routeMethod)
        assertEquals("path", result[11].name)
        assertEquals("/path", result[11].uri)
        assertEquals(parametersOf("key", "value"), result[11].parameters)
    }

    @Test
    fun shouldEmitLastCallAfterRestoration() = runTest {
        // GIVEN
        val job = Job()
        val stackManagerNotifier = FakeStackManagerNotifier()
        var previous: ApplicationCall? = null
        var result: ApplicationCall? = null

        StackManager.stackManagerNotifier = stackManagerNotifier

        routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            handle(path = "/path01", name = "path01") {
                result = call
            }

            handle(path = "/path02", name = "path02") {
                previous = previousCall()
                result = call
                job.complete()
            }

            // WHEN (Platform restored calls)
            stackManagerNotifier.restoration = """
                [
                    {
                        "name": "named",
                        "uri": "/path01",
                        "routeMethod": "PUSH",
                        "parameters": {}
                    },
                    {
                        "name": "",
                        "uri": "/path02",
                        "routeMethod": "PUSH",
                        "parameters": {
                            "key": ["value"]
                        }
                    }
                ]
            """.trimIndent()
        }
        advanceTimeBy(99)

        // THEN
        assertNotNull(previous)
        assertNotNull(result)
        assertEquals("/path01", "${previous?.uri}")
        assertEquals("named", "${previous?.name}")
        assertEquals(StackRouteMethod.Push, previous?.routeMethod)
        assertEquals(Parameters.Empty, previous?.parameters)
        assertEquals("/path02", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(parametersOf("key" to listOf("value")), result?.parameters)
    }
}
