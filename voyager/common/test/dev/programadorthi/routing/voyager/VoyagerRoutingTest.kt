package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.routing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class VoyagerRoutingTest {

    @Test
    fun shouldPushAScreen() = runTest {
        var screen: TestScreen? = null
        // GIVEN
        executeBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                install(VoyagerNavigator)

                push(path = "/path") {
                    TestScreen(value = "value").also {
                        screen = it
                        handled()
                    }
                }
            }
            // WHEN
            routing.push(path = "/path")
        }
        // THEN
        assertNotNull(screen)
        assertEquals(screen!!.value, "value")
    }

    @Test
    fun shouldReplaceAScreen() = runTest {
        var screen: TestScreen? = null
        // GIVEN
        executeBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                install(VoyagerNavigator)

                replace(path = "/path") {
                    TestScreen(value = "value").also {
                        screen = it
                        handled()
                    }
                }
            }
            // WHEN
            routing.replace(path = "/path")
        }
        // THEN
        assertNotNull(screen)
        assertEquals(screen!!.value, "value")
    }

    @Test
    fun shouldReplaceAllAScreen() = runTest {
        var screen: TestScreen? = null
        // GIVEN
        executeBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                install(VoyagerNavigator)

                replaceAll(path = "/path") {
                    TestScreen(value = "value").also {
                        screen = it
                        handled()
                    }
                }
            }
            // WHEN
            routing.replaceAll(path = "/path")
        }
        // THEN
        assertNotNull(screen)
        assertEquals(screen!!.value, "value")
    }

    @Test
    fun shouldPopAScreen() = runTest {
        val job = Job()
        val events = mutableListOf<VoyagerRouteEvent>()
        var screen: TestScreen? = null
        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(VoyagerNavigator)

            push(path = "/path") {
                TestScreen(value = "value").also {
                    screen = it
                }
            }

            pop(path = "/path") {
                job.complete()
            }
        }

        backgroundScope.launch(UnconfinedTestDispatcher()) {
            routing.application.voyagerEventManager.navigation.toList(events)
        }
        // WHEN
        routing.push(path = "/path")
        advanceTimeBy(99)
        routing.pop()
        advanceTimeBy(99)
        // THEN
        assertNotNull(screen)
        assertEquals(screen!!.value, "value")
        assertEquals(events.size, 3)
        assertIs<VoyagerRouteEvent.Idle>(events.first())
        assertIs<VoyagerRouteEvent.Push>(events[1])
        assertIs<VoyagerRouteEvent.Pop>(events.last())
    }

    private fun executeBody(
        body: CoroutineContext.(() -> Unit) -> Unit
    ) = runTest {
        val job = Job()
        (coroutineContext + job).body {
            job.cancel()
        }
        job.join()
    }
}
