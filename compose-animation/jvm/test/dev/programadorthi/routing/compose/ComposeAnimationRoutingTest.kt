package dev.programadorthi.routing.compose

import androidx.compose.ui.test.junit4.createComposeRule
import dev.programadorthi.routing.compose.helper.FakeContent
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
internal class ComposeAnimationRoutingTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun shouldInvokeInitialContentWhenThereIsNoEmittedComposable() {
        // GIVEN
        val routing = routing {}
        val fakeContent = FakeContent()

        // WHEN
        rule.setContent {
            Routing(
                routing = routing,
                initial = {
                    fakeContent.content = "I'm the initial content"
                    fakeContent.Composable()
                },
            )
        }
        rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

        // THEN
        assertEquals("I'm the initial content", fakeContent.result)
    }

    @Test
    fun shouldComposeByPath() =
        runTest {
            // GIVEN
            val job = Job()
            val fakeContent = FakeContent()

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/path") {
                        fakeContent.content = "I'm the path based content"
                        fakeContent.Composable()
                    }
                }

            rule.setContent {
                Routing(
                    routing = routing,
                    initial = {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    },
                )
            }

            // WHEN
            routing.call(uri = "/path", routeMethod = RouteMethod.Push)
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals("I'm the path based content", fakeContent.result)
        }

    @Test
    fun shouldComposeByName() =
        runTest {
            // GIVEN
            val job = Job()
            val fakeContent = FakeContent()

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/path", name = "path") {
                        fakeContent.content = "I'm the name based content"
                        fakeContent.Composable()
                    }
                }

            rule.setContent {
                Routing(
                    routing = routing,
                    initial = {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    },
                )
            }

            // WHEN
            routing.call(name = "path", routeMethod = RouteMethod.Push)
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals("I'm the name based content", fakeContent.result)
        }

    @Test
    fun shouldComposeByAnyRoute() =
        runTest {
            // GIVEN
            val job = Job()
            val fakeContent = FakeContent()

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    route(path = "/any") {
                        composable {
                            fakeContent.content = "I'm the generic based content"
                            fakeContent.Composable()
                        }
                    }
                }

            rule.setContent {
                Routing(
                    routing = routing,
                    initial = {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    },
                )
            }

            // WHEN
            routing.call(uri = "/any", routeMethod = RouteMethod.Push)
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals("I'm the generic based content", fakeContent.result)
        }

    @Test
    fun shouldPushAComposable() =
        runTest {
            // GIVEN
            val job = Job()
            val fakeContent = FakeContent()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/path") {
                        result = call
                        fakeContent.content = "I'm the push based content"
                        fakeContent.Composable()
                    }
                }

            rule.setContent {
                Routing(
                    routing = routing,
                    initial = {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    },
                )
            }

            // WHEN
            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

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
        runTest {
            // GIVEN
            val job = Job()
            val pushContent = FakeContent()
            val replaceContent = FakeContent()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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

            rule.setContent {
                Routing(
                    routing = routing,
                    initial = {
                        replaceContent.content = "I'm the initial content"
                        replaceContent.Composable()
                    },
                )
            }

            // WHEN
            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.replace(path = "/replace")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

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
        runTest {
            // GIVEN
            val job = Job()
            val pushContent = FakeContent()
            val replaceContent = FakeContent()
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
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

            rule.setContent {
                Routing(
                    routing = routing,
                    initial = {
                        replaceContent.content = "I'm the initial content"
                        replaceContent.Composable()
                    },
                )
            }

            // WHEN
            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.replaceAll(path = "/replace")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertNotNull(result)
            assertEquals("I'm the push based content", pushContent.result)
            assertEquals("I'm the replace all based content", replaceContent.result)
            assertEquals("/replace", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.ReplaceAll, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldPopAComposable() =
        runTest {
            // GIVEN
            val job = Job()
            var composedCounter = 0

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/push") {
                        composedCounter += 1
                    }
                }

            rule.setContent {
                Routing(
                    routing = routing,
                    initial = {},
                )
            }

            // WHEN
            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(99L) // Ask for recomposition

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(99L) // Ask for recomposition

            routing.pop()
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals(5, composedCounter)
        }

    @Test
    fun shouldPopAComposableWithResult() =
        runTest {
            // GIVEN
            val job = Job()
            var poppedMessage: String? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/push") {
                        poppedMessage = LocalRouting.current.poppedEntry()?.popResult<String>()
                    }
                }

            rule.setContent {
                Routing(
                    routing = routing,
                    initial = {},
                )
            }

            // WHEN
            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.pop(result = "This is the popped message")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals("This is the popped message", poppedMessage)
        }

    @Test
    fun shouldPopResultBeNullAfterANewRouting() =
        runTest {
            // GIVEN
            val job = Job()
            var poppedMessage: String? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/push") {
                        poppedMessage = LocalRouting.current.poppedEntry()?.popResult<String>()
                    }
                }

            rule.setContent {
                Routing(
                    routing = routing,
                    initial = {},
                )
            }

            // WHEN
            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.pop(result = "This is the popped message")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertNull(poppedMessage, "Pop result should be cleared after other routing call")
        }
}
