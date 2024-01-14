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
    fun shouldFailWhenMissingStackPlugin() =
        runTest {
            // GIVEN
            val routing =
                routing {
                    route(path = "/path") {}
                }

            // WHEN
            val result =
                assertFails {
                    routing.push(path = "/path")
                }

            // THEN
            assertIs<IllegalStateException>(result)
            assertEquals("StackRouting plugin not installed", result.message)
        }

    @Test
    fun shouldPushAPath() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldPushAPathWithParameters() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf("key", "value"), result?.parameters)
        }

    @Test
    fun shouldPushAPathByName() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldPushAPathByNameWithParameters() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf("key", "value"), result?.parameters)
        }

    @Test
    fun shouldPushAPathByNameWithPathParameters() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf("id", "123"), result?.parameters)
        }

    @Test
    fun shouldPopWithParameters() =
        runTest {
            // GIVEN
            val job = Job()
            val result = mutableListOf<ApplicationCall>()

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result[0].routeMethod)
            assertEquals(Parameters.Empty, result[0].parameters)
            // Second handle is a pop call
            assertEquals("/path", result[1].uri)
            assertEquals("", result[1].name)
            assertEquals(RouteMethod.Pop, result[1].routeMethod)
            assertEquals(parametersOf("key", "value"), result[1].parameters)
        }

    @Test
    fun shouldPopNamedRoutes() =
        runTest {
            // GIVEN
            val job = Job()
            val result = mutableListOf<ApplicationCall>()

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result[0].routeMethod)
            assertEquals(Parameters.Empty, result[0].parameters)
            // Second handle is a pop call
            assertEquals("/path", result[1].uri)
            assertEquals("path", result[1].name)
            assertEquals(RouteMethod.Pop, result[1].routeMethod)
            assertEquals(parametersOf("key", "value"), result[1].parameters)
        }

    @Test
    fun shouldCreateARouteUsingHandleDirectly() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldReplaceAPath() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Replace, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldReplaceAllPaths() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.ReplaceAll, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldRedirectToOtherRouteByPath() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(StackRouting)

                    route(path = "/path") {
                        handle {
                            if (!call.isPop()) {
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
            assertEquals(RouteMethod.Replace, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldRedirectToOtherRouteByName() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldRouteByRegex() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf("number", "123"), result?.parameters)
        }

    @Test
    fun shouldNeglectARouteWhenFlagged() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf(), result?.parameters)
        }

    @Test
    fun shouldNotNeglectARouteWhenNotFlagged() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf(), result?.parameters)
        }

    @Test
    fun shouldPushParentPathWhenRoutingFromChild() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val parent =
                routing(
                    parentCoroutineContext = coroutineContext + job,
                ) {
                    install(StackRouting)

                    route(path = "/pathParent") {
                        handle {
                            result = call
                            job.complete()
                        }
                    }
                }

            val routing =
                routing(
                    rootPath = "/child",
                    parent = parent,
                    parentCoroutineContext = coroutineContext + job,
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldPushChildPathWhenRoutingFromParent() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val parent =
                routing(
                    parentCoroutineContext = coroutineContext + job,
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
                parentCoroutineContext = coroutineContext + job,
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldPreviousCallBeNull() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(StackRouting)

                    route(path = "/path", name = "path") {
                        handle {
                            result = call.previousCall
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
    fun shouldPreviousCallNotBeNull() =
        runTest {
            // GIVEN
            val job = Job()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(StackRouting)

                    route(path = "/path", name = "path") {
                        handle {}
                    }

                    route(path = "/path2", name = "path2") {
                        handle {
                            result = call.previousCall
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
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf(), result?.parameters)
        }

    @Test
    fun shouldInstallStackManagerRegisterIt() =
        runTest {
            // GIVEN
            val stackManagerNotifier = FakeStackManagerNotifier()
            StackManager.stackManagerNotifier = stackManagerNotifier

            val routing =
                routing {
                    // WHEN
                    install(StackRouting)
                }

            // THEN
            val environment = routing.environment
            val providerId = "${environment?.parentRouting}#${environment?.rootPath}"
            assertEquals(providerId, stackManagerNotifier.subscriptions.keys.first())
        }

    @Test
    fun shouldDisposeRoutingUnRegisterStackManager() =
        runTest {
            // GIVEN
            val stackManagerNotifier = FakeStackManagerNotifier()
            StackManager.stackManagerNotifier = stackManagerNotifier

            val routing =
                routing {
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
    fun shouldHandleAnyStackAction() =
        runTest {
            // GIVEN
            val job = Job()
            val result = mutableListOf<ApplicationCall>()

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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
            assertEquals(12, result.size)
            // First handle is a push call
            val firstPushCall = result[0]
            assertEquals(RouteMethod.Push, firstPushCall.routeMethod)
            assertEquals("", firstPushCall.name)
            assertEquals("/path", firstPushCall.uri)
            assertEquals(Parameters.Empty, firstPushCall.parameters)
            assertNull(
                firstPushCall.previousCall,
                message = "Expect first push have no previous call",
            )
            // Second handle is a push call
            val firstPushNamedCall = result[1]
            assertEquals(RouteMethod.Push, firstPushNamedCall.routeMethod)
            assertEquals("path", firstPushNamedCall.name)
            assertEquals("/path", firstPushNamedCall.uri)
            assertEquals(Parameters.Empty, firstPushNamedCall.parameters)
            assertEquals(
                firstPushCall.toCompare(),
                firstPushNamedCall.previousCall,
                message = "Expect first push named have first push as previous call",
            )
            // Third handle is a replace call
            val firstReplaceCall = result[2]
            assertEquals(RouteMethod.Replace, firstReplaceCall.routeMethod)
            assertEquals("", firstReplaceCall.name)
            assertEquals("/path", firstReplaceCall.uri)
            assertEquals(Parameters.Empty, firstReplaceCall.parameters)
            assertEquals(
                firstPushNamedCall.toCompare(),
                firstReplaceCall.previousCall,
                message = "Expect first replace have first push named as previous call",
            )
            // Fourth handle is a pop call emitted by replace
            val firstPopByReplaceCall = result[3]
            assertEquals(RouteMethod.Pop, firstPopByReplaceCall.routeMethod)
            assertEquals("path", firstPopByReplaceCall.name)
            assertEquals("/path", firstPopByReplaceCall.uri)
            assertEquals(Parameters.Empty, firstPopByReplaceCall.parameters)
            assertEquals(
                firstReplaceCall.toCompare(),
                firstPopByReplaceCall.previousCall,
                message = "Expect first pop by replace have first replace as previous call",
            )
            // Fifth handle is a replace call
            val firstReplaceNamedCall = result[4]
            assertEquals(RouteMethod.Replace, firstReplaceNamedCall.routeMethod)
            assertEquals("path", firstReplaceNamedCall.name)
            assertEquals("/path", firstReplaceNamedCall.uri)
            assertEquals(Parameters.Empty, firstReplaceNamedCall.parameters)
            assertEquals(
                firstReplaceCall.toCompare(),
                firstReplaceNamedCall.previousCall,
                message = "Expect first replace named have first replace as previous call",
            )
            // Sixth handle is a pop call emitted by replace
            val firstPopByReplaceNamedCall = result[5]
            assertEquals(RouteMethod.Pop, firstPopByReplaceNamedCall.routeMethod)
            assertEquals("", firstPopByReplaceNamedCall.name)
            assertEquals("/path", firstPopByReplaceNamedCall.uri)
            assertEquals(Parameters.Empty, firstPopByReplaceNamedCall.parameters)
            assertEquals(
                firstReplaceNamedCall.toCompare(),
                firstPopByReplaceNamedCall.previousCall,
                message = "Expect first pop by replace named have first replace named as previous call",
            )
            // Seventh handle is a replace all call
            val firstReplaceAllCall = result[6]
            assertEquals(RouteMethod.ReplaceAll, firstReplaceAllCall.routeMethod)
            assertEquals("", firstReplaceAllCall.name)
            assertEquals("/path", firstReplaceAllCall.uri)
            assertEquals(Parameters.Empty, firstReplaceAllCall.parameters)
            assertEquals(
                firstReplaceNamedCall.toCompare(),
                firstReplaceAllCall.previousCall,
                message = "Expect first replace all have first replace named as previous call",
            )
            // Eighth handle is a pop call emitted by replace all
            val firstPopByReplaceAllCall = result[7]
            assertEquals(RouteMethod.Pop, firstPopByReplaceAllCall.routeMethod)
            assertEquals("path", firstPopByReplaceAllCall.name)
            assertEquals("/path", firstPopByReplaceAllCall.uri)
            assertEquals(Parameters.Empty, firstPopByReplaceAllCall.parameters)
            assertEquals(
                firstReplaceAllCall.toCompare(),
                firstPopByReplaceAllCall.previousCall,
                message = "Expect first pop by replace all have first replace all as previous call",
            )
            // Ninth handle is a pop call emitted by replace all
            val secondPopByReplaceAllCall = result[8]
            assertEquals(RouteMethod.Pop, secondPopByReplaceAllCall.routeMethod)
            assertEquals("", secondPopByReplaceAllCall.name)
            assertEquals("/path", secondPopByReplaceAllCall.uri)
            assertEquals(Parameters.Empty, secondPopByReplaceAllCall.parameters)
            assertEquals(
                firstReplaceNamedCall.toCompare(),
                secondPopByReplaceAllCall.previousCall,
                message = "Expect second pop by replace all have first replace named as previous call",
            )
            // Tenth handle is a replace all call
            val firstReplaceAllNamedCall = result[9]
            assertEquals(RouteMethod.ReplaceAll, firstReplaceAllNamedCall.routeMethod)
            assertEquals("path", firstReplaceAllNamedCall.name)
            assertEquals("/path", firstReplaceAllNamedCall.uri)
            assertEquals(Parameters.Empty, firstReplaceAllNamedCall.parameters)
            assertEquals(
                firstReplaceAllCall.toCompare(),
                firstReplaceAllNamedCall.previousCall,
                message = "Expect first replace all named have first replace all as previous call",
            )
            // Eleventh handle is a pop call emitted by replace all
            val firstPopByReplaceAllNamedCall = result[10]
            assertEquals(RouteMethod.Pop, firstPopByReplaceAllNamedCall.routeMethod)
            assertEquals("", firstPopByReplaceAllNamedCall.name)
            assertEquals("/path", firstPopByReplaceAllNamedCall.uri)
            assertEquals(Parameters.Empty, firstPopByReplaceAllNamedCall.parameters)
            assertEquals(
                firstReplaceAllNamedCall.toCompare(),
                firstPopByReplaceAllNamedCall.previousCall,
                message = "Expect first pop by replace all named have first replace all named as previous call",
            )
            // Twelfth handle is a pop call
            val firstPopCall = result[11]
            assertEquals(RouteMethod.Pop, firstPopCall.routeMethod)
            assertEquals("path", firstPopCall.name)
            assertEquals("/path", firstPopCall.uri)
            assertEquals(parametersOf("key", "value"), firstPopCall.parameters)
            assertNull(
                firstPopCall.previousCall,
                message = "Expect pop have no previous call",
            )
        }

    @Test
    fun shouldEmitLastCallAfterRestoration() =
        runTest {
            // GIVEN
            val job = Job()
            val stackManagerNotifier = FakeStackManagerNotifier()
            var result: ApplicationCall? = null

            StackManager.stackManagerNotifier = stackManagerNotifier

            routing(parentCoroutineContext = coroutineContext + job) {
                install(StackRouting)

                handle(path = "/path01", name = "path01") {
                    result = call
                }

                handle(path = "/path02", name = "path02") {
                    result = call
                    job.complete()
                }

                // WHEN (Platform restored calls)
                stackManagerNotifier.restoration =
                    """
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
            assertNotNull(result)

            assertEquals("/path01", "${result?.previousCall?.uri}")
            assertEquals("named", "${result?.previousCall?.name}")
            assertEquals(RouteMethod.Push, result?.previousCall?.routeMethod)
            assertEquals(Parameters.Empty, result?.previousCall?.parameters)

            assertEquals("/path02", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf("key" to listOf("value")), result?.parameters)
        }

    @Test
    fun shouldManyOperationsHandlePreviousCall() =
        runTest {
            // GIVEN
            val job = Job()
            val result = mutableListOf<ApplicationCall>()

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(StackRouting)

                    handle(path = "/path1", name = "path1") {
                        result += call
                    }

                    handle(path = "/path2", name = "path2") {
                        result += call
                    }

                    handle(path = "/path3", name = "path3") {
                        result += call
                    }
                }

            // WHEN
            routing.push(path = "/path1")
            advanceTimeBy(99)
            routing.push(path = "/path2")
            advanceTimeBy(99)
            routing.pop()
            advanceTimeBy(99)
            routing.push(path = "/path3")
            advanceTimeBy(99)
            routing.pop()
            advanceTimeBy(99)

            // THEN
            assertEquals(7, result.size)
            // First handle is a push call
            val firstPushCall = result[0]
            assertEquals(RouteMethod.Push, firstPushCall.routeMethod)
            assertEquals("", firstPushCall.name)
            assertEquals("/path1", firstPushCall.uri)
            assertEquals(Parameters.Empty, firstPushCall.parameters)
            assertNull(
                firstPushCall.previousCall,
                message = "Expect first push have no previous call",
            )
            // Second handle is a push call
            val secondPushCall = result[1]
            assertEquals(RouteMethod.Push, secondPushCall.routeMethod)
            assertEquals("", secondPushCall.name)
            assertEquals("/path2", secondPushCall.uri)
            assertEquals(Parameters.Empty, secondPushCall.parameters)
            assertEquals(
                firstPushCall.toCompare(),
                secondPushCall.previousCall,
                message = "Expect second push have first push as previous call",
            )
            // Third handle is a pop call
            val firstPopCall = result[2]
            assertEquals(RouteMethod.Pop, firstPopCall.routeMethod)
            assertEquals("", firstPopCall.name)
            assertEquals("/path2", firstPopCall.uri)
            assertEquals(Parameters.Empty, firstPopCall.parameters)
            assertEquals(
                firstPushCall.toCompare(),
                firstPopCall.previousCall,
                message = "Expect first pop have first push as previous call",
            )
            // Fourth handle is a push emitted by pop
            val firstPushByPopCall = result[3]
            assertEquals(RouteMethod.Push, firstPushByPopCall.routeMethod)
            assertEquals("", firstPushByPopCall.name)
            assertEquals("/path1", firstPushByPopCall.uri)
            assertEquals(Parameters.Empty, firstPushByPopCall.parameters)
            assertEquals(
                firstPopCall.toCompare(),
                firstPushByPopCall.previousCall,
                message = "Expect first push called by pop operation have first pop as previous call",
            )
            // Fifth handle is a replace call
            val thirdPushCall = result[4]
            assertEquals(RouteMethod.Push, thirdPushCall.routeMethod)
            assertEquals("", thirdPushCall.name)
            assertEquals("/path3", thirdPushCall.uri)
            assertEquals(Parameters.Empty, thirdPushCall.parameters)
            assertEquals(
                firstPushCall.toCompare(),
                thirdPushCall.previousCall,
                message = "Expect third push have first push as previous call",
            )
            // Sixth handle is a pop call
            val secondPopCall = result[5]
            assertEquals(RouteMethod.Pop, secondPopCall.routeMethod)
            assertEquals("", secondPopCall.name)
            assertEquals("/path3", secondPopCall.uri)
            assertEquals(Parameters.Empty, secondPopCall.parameters)
            assertEquals(
                firstPushCall.toCompare(),
                secondPopCall.previousCall,
                message = "Expect second pop have first push as previous call",
            )
            // Fourth handle is a push emitted by pop
            val secondPushByPopCall = result[6]
            assertEquals(RouteMethod.Push, secondPushByPopCall.routeMethod)
            assertEquals("", secondPushByPopCall.name)
            assertEquals("/path1", secondPushByPopCall.uri)
            assertEquals(Parameters.Empty, secondPushByPopCall.parameters)
            assertEquals(
                secondPopCall.toCompare(),
                secondPushByPopCall.previousCall,
                message = "Expect second push called by pop operation have second pop as previous call",
            )
        }

    private fun ApplicationCall.toCompare(): ApplicationCall =
        ApplicationCall(
            application = application,
            name = name,
            uri = if (name.isBlank()) uri else "",
            routeMethod = routeMethod,
            attributes = attributes,
            parameters = parameters,
        )
}
