package dev.programadorthi.routing.compose.animation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.test.junit4.createComposeRule
import dev.programadorthi.routing.compose.composable
import dev.programadorthi.routing.compose.pop
import dev.programadorthi.routing.compose.popped
import dev.programadorthi.routing.compose.poppedCall
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.routing
import io.ktor.http.Parameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeAnimationRoutingTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun shouldAnimateUsingGlobalEnterAndExitTransitions() =
        runTest {
            // GIVEN
            val job = Job()
            var previous: ApplicationCall? = null
            var next: ApplicationCall? = null
            var exitPrevious: ApplicationCall? = null
            var exitNext: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/initial") { }
                    composable(path = "/path") { }
                }

            // WHEN
            rule.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                    enterTransition = {
                        previous = initialState
                        next = targetState
                        fadeIn()
                    },
                    exitTransition = {
                        exitPrevious = initialState
                        exitNext = targetState
                        fadeOut()
                    },
                )
            }

            advanceTimeBy(99) // Ask for start uri routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals(previous, exitPrevious)
            assertEquals(next, exitNext)
            assertEquals("/initial", "${previous?.uri}")
            assertEquals("", "${previous?.name}")
            assertEquals(RouteMethod.Push, previous?.routeMethod)
            assertEquals(Parameters.Empty, previous?.parameters)
            assertEquals("/path", "${next?.uri}")
            assertEquals("", "${next?.name}")
            assertEquals(RouteMethod.Push, next?.routeMethod)
            assertEquals(Parameters.Empty, next?.parameters)
        }

    @Test
    fun shouldAnimateUsingGlobalPopEnterAndPopExitTransitions() =
        runTest {
            // GIVEN
            val job = Job()
            var previous: ApplicationCall? = null
            var next: ApplicationCall? = null
            var exitPrevious: ApplicationCall? = null
            var exitNext: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/initial") { }
                    composable(path = "/path") { }
                }

            // WHEN
            rule.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() },
                    popEnterTransition = {
                        previous = initialState
                        next = targetState
                        fadeIn()
                    },
                    popExitTransition = {
                        exitPrevious = initialState
                        exitNext = targetState
                        fadeOut()
                    },
                )
            }

            advanceTimeBy(99) // Ask for start uri routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.pop()
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals(previous, exitPrevious)
            assertEquals(next, exitNext)
            assertEquals("/path", "${previous?.uri}")
            assertEquals("", "${previous?.name}")
            assertEquals(RouteMethod.Push, previous?.routeMethod)
            assertEquals(Parameters.Empty, previous?.parameters)
            assertEquals("/initial", "${next?.uri}")
            assertEquals("", "${next?.name}")
            assertEquals(RouteMethod.Push, next?.routeMethod)
            assertEquals(Parameters.Empty, next?.parameters)
            assertEquals(true, previous?.popped, "Previous call should be popped")
            assertEquals(
                routing.poppedCall(),
                previous,
                "Previous call should be equals to popped call",
            )
        }

    @Test
    fun shouldAnimateUsingLocalEnterTransitions() =
        runTest {
            // GIVEN
            val job = Job()
            var previous: ApplicationCall? = null
            var next: ApplicationCall? = null
            var exitPrevious: ApplicationCall? = null
            var exitNext: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/initial") { }
                    composable(
                        path = "/path",
                        enterTransition = {
                            previous = initialState
                            next = targetState
                            fadeIn()
                        },
                    ) {
                    }
                }

            // WHEN
            rule.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                    enterTransition = { fadeIn() },
                    // Initial screen always uses global enter and exit transition
                    exitTransition = {
                        exitPrevious = initialState
                        exitNext = targetState
                        fadeOut()
                    },
                )
            }

            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals(previous, exitPrevious)
            assertEquals(next, exitNext)
            assertEquals("/initial", "${previous?.uri}")
            assertEquals("", "${previous?.name}")
            assertEquals(RouteMethod.Push, previous?.routeMethod)
            assertEquals(Parameters.Empty, previous?.parameters)
            assertEquals("/path", "${next?.uri}")
            assertEquals("", "${next?.name}")
            assertEquals(RouteMethod.Push, next?.routeMethod)
            assertEquals(Parameters.Empty, next?.parameters)
        }

    @Test
    fun shouldAnimateUsingLocalEnterAndExitTransitions() =
        runTest {
            // GIVEN
            val job = Job()
            var previous: ApplicationCall? = null
            var next: ApplicationCall? = null
            var exitPrevious: ApplicationCall? = null
            var exitNext: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/initial") { }
                    composable(
                        path = "/path",
                        method = RouteMethod.Push,
                        enterTransition = {
                            previous = initialState
                            next = targetState
                            fadeIn()
                        },
                        exitTransition = {
                            exitPrevious = initialState
                            exitNext = targetState
                            fadeOut()
                        },
                    ) {
                    }
                }

            // WHEN
            rule.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() },
                )
            }

            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.pop()
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals(previous, exitNext)
            assertEquals(next, exitPrevious)
            assertEquals("/initial", "${exitNext?.uri}")
            assertEquals("", "${exitNext?.name}")
            assertEquals(RouteMethod.Push, exitNext?.routeMethod)
            assertEquals(Parameters.Empty, exitNext?.parameters)
            assertEquals("/path", "${exitPrevious?.uri}")
            assertEquals("", "${exitPrevious?.name}")
            assertEquals(RouteMethod.Push, exitPrevious?.routeMethod)
            assertEquals(Parameters.Empty, exitPrevious?.parameters)
        }

    @Test
    fun shouldInitialContentBeCalledWithTransitions() =
        runTest {
            // GIVEN
            val job = Job()
            var previous: ApplicationCall? = null
            var next: ApplicationCall? = null
            var exitPrevious: ApplicationCall? = null
            var exitNext: ApplicationCall? = null
            var initialContent = ""

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/initial") {
                        initialContent = "this is the initial content"
                    }
                }

            // WHEN
            rule.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                    enterTransition = {
                        previous = initialState
                        next = targetState
                        fadeIn()
                    },
                    exitTransition = {
                        exitPrevious = initialState
                        exitNext = targetState
                        fadeOut()
                    },
                )
            }

            advanceTimeBy(99)
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals("this is the initial content", initialContent)
            assertEquals(previous, exitNext)
            assertEquals(next, exitPrevious)
        }

    @Test
    fun shouldInitialContentNotBeCalledWithTransitionsInASecondTime() =
        runTest {
            // GIVEN
            val job = Job()
            var previous: ApplicationCall? = null
            var next: ApplicationCall? = null
            var exitPrevious: ApplicationCall? = null
            var exitNext: ApplicationCall? = null
            var initialContentCount = 0

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    composable(path = "/initial") {
                        initialContentCount += 1
                    }
                    composable(path = "/path") {
                    }
                }

            // WHEN
            rule.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                    enterTransition = {
                        previous = initialState
                        next = targetState
                        fadeIn()
                    },
                    exitTransition = {
                        exitPrevious = initialState
                        exitNext = targetState
                        fadeOut()
                    },
                )
            }

            // Render initial content
            advanceTimeBy(99)
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // Go to other composition
            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // Back to initial content
            routing.pop()
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals(3, initialContentCount)
            assertEquals(previous, exitPrevious)
            assertEquals(next, exitNext)
            assertEquals("/path", "${previous?.uri}")
            assertEquals("", "${previous?.name}")
            assertEquals(RouteMethod.Push, previous?.routeMethod)
            assertEquals(Parameters.Empty, previous?.parameters)
            assertEquals("/initial", "${next?.uri}")
            assertEquals("", "${next?.name}")
            assertEquals(RouteMethod.Push, next?.routeMethod)
            assertEquals(Parameters.Empty, next?.parameters)
            assertEquals(true, previous?.popped, "Previous call should be popped")
            assertEquals(
                routing.poppedCall(),
                previous,
                "Previous call should be equals to popped call",
            )
        }
}
