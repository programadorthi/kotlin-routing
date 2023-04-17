package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll
import dev.programadorthi.routing.core.routing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class VoyagerRoutingTest {

    @Test
    fun shouldPushAScreen() = runTest {
        // GIVEN
        val job = Job()
        var screen: TestScreen? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(VoyagerNavigator)

            screen(path = "/path") {
                TestScreen(value = "value").also {
                    screen = it
                    job.complete()
                }
            }
        }

        // WHEN
        routing.push(path = "/path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(screen)
        assertEquals(screen!!.value, "value")
    }

    @Test
    fun shouldReplaceAScreen() = runTest {
        // GIVEN
        val job = Job()
        var screen: TestScreen? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(VoyagerNavigator)

            screen(path = "/path") {
                TestScreen(value = "value").also {
                    screen = it
                    job.complete()
                }
            }
        }

        // WHEN
        routing.replace(path = "/path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(screen)
        assertEquals(screen!!.value, "value")
    }

    @Test
    fun shouldReplaceAllAScreen() = runTest {
        // GIVEN
        val job = Job()
        var screen: TestScreen? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(VoyagerNavigator)

            screen(path = "/path") {
                TestScreen(value = "value").also {
                    screen = it
                    job.complete()
                }
            }
        }

        // WHEN
        routing.replaceAll(path = "/path")
        advanceTimeBy(99)

        // THEN
        assertNotNull(screen)
        assertEquals(screen!!.value, "value")
    }

    @Test
    fun shouldPopAScreen() = runTest {
        // GIVEN
        val job = Job()
        val sequence = mutableListOf<String>()

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(VoyagerNavigator)

            screen(path = "/path") {
                TestScreen(value = "value").also {
                    sequence += "pushed screen"
                }
            }

            pop(path = "/path") {
                sequence += "popped screen"
                job.complete()
            }
        }

        // WHEN
        routing.push(path = "/path")
        advanceTimeBy(99)
        routing.pop()
        advanceTimeBy(99)

        // THEN
        assertEquals(listOf("pushed screen", "popped screen"), sequence)
    }
}
