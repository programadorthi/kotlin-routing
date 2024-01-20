package dev.programadorthi.routing.compose

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.test.junit4.createComposeRule
import dev.programadorthi.routing.compose.animation.Routing
import dev.programadorthi.routing.compose.animation.composable
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
internal class ComposeAnimationRoutingTest {
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
                    composable(path = "/path") {
                    }
                }

            // WHEN
            rule.setContent {
                Routing(
                    routing = routing,
                    initial = { },
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

            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals(previous, exitPrevious)
            assertEquals(next, exitNext)
            assertEquals("/", "${previous?.uri}")
            assertEquals("", "${previous?.name}")
            assertEquals(RouteMethod.Empty, previous?.routeMethod)
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
                    composable(path = "/path") {
                    }
                }

            // WHEN
            rule.setContent {
                Routing(
                    routing = routing,
                    initial = { },
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
            assertEquals("/", "${next?.uri}")
            assertEquals("", "${next?.name}")
            assertEquals(RouteMethod.Empty, next?.routeMethod)
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
                    initial = { },
                    // Initial screen always uses global enter and exit transition
                    exitTransition = {
                        exitPrevious = initialState
                        exitNext = targetState
                        fadeOut()
                    },
                )
            }

            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals(previous, exitPrevious)
            assertEquals(next, exitNext)
            assertEquals("/", "${previous?.uri}")
            assertEquals("", "${previous?.name}")
            assertEquals(RouteMethod.Empty, previous?.routeMethod)
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
                    composable(
                        path = "/path",
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
                    initial = { },
                )
            }

            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            routing.pop()
            rule.mainClock.advanceTimeBy(0L) // Ask for recomposition

            // THEN
            assertEquals(previous, exitNext)
            assertEquals(next, exitPrevious)
            assertEquals("/", "${exitNext?.uri}")
            assertEquals("", "${exitNext?.name}")
            assertEquals(RouteMethod.Empty, exitNext?.routeMethod)
            assertEquals(Parameters.Empty, exitNext?.parameters)
            assertEquals("/path", "${exitPrevious?.uri}")
            assertEquals("", "${exitPrevious?.name}")
            assertEquals(RouteMethod.Push, exitPrevious?.routeMethod)
            assertEquals(Parameters.Empty, exitPrevious?.parameters)
        }
}
