package dev.programadorthi.routing.compose

import dev.programadorthi.routing.compose.helper.FakeContent
import dev.programadorthi.routing.compose.helper.runComposeTest
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.core.routing
import io.ktor.http.Parameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeRoutingTest {
    @Test
    fun shouldInvokeInitialContentWhenThereIsNoEmittedComposable() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    composable(path = "/initial") {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    }
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99) // Ask for start uri routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("I'm the initial content", fakeContent.result)
            assertFalse(routing.canPop, "pop having one call only isn't valid")
        }

    @Test
    fun shouldComposeByPath() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    composable(path = "/initial") {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    }
                    composable(path = "/path") {
                        fakeContent.content = "I'm the path based content"
                        fakeContent.Composable()
                    }
                }

            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }

            // WHEN
            advanceTimeBy(99) // Ask for start uri routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.call(uri = "/path", routeMethod = RouteMethod.Push)
            advanceTimeBy(99) // Ask for /path routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("I'm the path based content", fakeContent.result)
            assertTrue(routing.canPop, "pop should be available having more than one call")
        }

    @Test
    fun shouldComposeByName() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    composable(path = "/initial") {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    }
                    composable(path = "/path", name = "path") {
                        fakeContent.content = "I'm the name based content"
                        fakeContent.Composable()
                    }
                }

            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }

            // WHEN
            advanceTimeBy(99) // Ask for start uri routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.call(name = "path", routeMethod = RouteMethod.Push)
            advanceTimeBy(99) // Ask for /path routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("I'm the name based content", fakeContent.result)
        }

    @Test
    fun shouldComposeByAnyRoute() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    composable(path = "/initial") {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    }
                    route(path = "/any") {
                        composable {
                            fakeContent.content = "I'm the generic based content"
                            fakeContent.Composable()
                        }
                    }
                }

            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }

            // WHEN
            advanceTimeBy(99) // Ask for start uri routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.call(uri = "/any", routeMethod = RouteMethod.Push)
            advanceTimeBy(99) // Ask /any for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("I'm the generic based content", fakeContent.result)
        }

    @Test
    fun shouldPushAComposable() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    composable(path = "/initial") {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    }
                    composable(path = "/path") {
                        result = call
                        fakeContent.content = "I'm the push based content"
                        fakeContent.Composable()
                    }
                }

            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }

            // WHEN
            advanceTimeBy(99) // Ask for start uri routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for /path routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(result)
            assertEquals("I'm the push based content", fakeContent.result)
            assertEquals("/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldReplaceAComposable() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val pushContent = FakeContent()
            val replaceContent = FakeContent()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    composable(path = "/initial") {
                        pushContent.content = "I'm the initial content"
                        pushContent.Composable()
                    }
                    composable(path = "/push") {
                        pushContent.content = "I'm the push based content"
                        pushContent.Composable()
                    }
                    composable(path = "/replace") {
                        result = call
                        replaceContent.content = "I'm the replace based content"
                        replaceContent.Composable()
                    }
                }

            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }

            // WHEN
            advanceTimeBy(99) // Ask for start uri routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for /push routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.replace(path = "/replace")
            advanceTimeBy(99) // Ask for /replace routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(result)
            assertEquals("I'm the push based content", pushContent.result)
            assertEquals("I'm the replace based content", replaceContent.result)
            assertEquals("/replace", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Replace, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldReplaceAllComposable() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val pushContent = FakeContent()
            val replaceContent = FakeContent()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    composable(path = "/initial") {
                        pushContent.content = "I'm the initial content"
                        pushContent.Composable()
                    }
                    composable(path = "/push") {
                        pushContent.content = "I'm the push based content"
                        pushContent.Composable()
                    }
                    composable(path = "/replace") {
                        result = call
                        replaceContent.content = "I'm the replace all based content"
                        replaceContent.Composable()
                    }
                }

            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }

            // WHEN
            advanceTimeBy(99) // Ask for start uri routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for /push routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.replaceAll(path = "/replace")
            advanceTimeBy(99) // Ask for /replace routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(result)
            assertEquals("I'm the push based content", pushContent.result)
            assertEquals("I'm the replace all based content", replaceContent.result)
            assertEquals("/replace", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.ReplaceAll, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
            assertFalse(routing.canPop, "replace all should turn can pop false")
        }

    @Test
    fun shouldPopAComposable() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var composedCounter = -1

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    composable(path = "/initial") {
                        composedCounter = 0
                    }
                    composable(path = "/push") {
                        composedCounter += 1
                    }
                }

            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }

            // WHEN
            advanceTimeBy(99) // Ask for start uri routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for /push routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for /push routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.pop()
            advanceTimeBy(99) // Ask for pop routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(3, composedCounter)
        }

    @Test
    fun shouldPopAComposableWithResult() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var poppedMessage: String? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    composable(path = "/initial") {
                        poppedMessage = "I'm the initial content"
                    }
                    composable(path = "/push") {
                        poppedMessage = LocalRouting.current.poppedCall?.popResult<String>()
                    }
                }

            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }

            // WHEN
            advanceTimeBy(99) // Ask for start uri routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for /push routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for /push routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.pop(result = "This is the popped message")
            advanceTimeBy(99) // Ask for pop routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("This is the popped message", poppedMessage)
        }

    @Test
    fun shouldPopResultBeNullAfterANewRouting() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var poppedMessage: String? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    composable(path = "/initial") {
                        poppedMessage = "I'm the initial content"
                    }
                    composable(path = "/push") {
                        poppedMessage = LocalRouting.current.poppedCall?.popResult<String>()
                    }
                }

            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }

            // WHEN
            advanceTimeBy(99) // Ask for start uri routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for /push routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.pop(result = "This is the popped message")
            advanceTimeBy(99) // Ask for pop routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for /push routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNull(poppedMessage, "Pop result should be cleared after other routing call")
        }
}
