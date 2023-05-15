package dev.programadorthi.routing.events

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.core.routing
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class EventRoutingTest {

    @Test
    fun shouldRedirectFromAnEventToAnother() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            event(name = "event_name") {
                call.redirectToEvent(name = "event_redirected")
            }

            event(name = "event_redirected") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.emitEvent(name = "event_name")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("event_redirected", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(EventRouteMethod, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldHandleAnEvent() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            event(name = "event_name") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.emitEvent(name = "event_name")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("event_name", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(EventRouteMethod, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldHandleAnEventWithParameters() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            event(name = "event_name") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.emitEvent(
            name = "event_name",
            parameters = parametersOf("id", "123"),
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("event_name", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(EventRouteMethod, result?.routeMethod)
        assertEquals(parametersOf("id", "123"), result?.parameters)
    }

    @Test
    fun shouldThrowExceptionPuttingEventAsSubRoute() = runTest {
        // WHEN
        val failure = assertFails {
            routing {
                route(path = "/path") {
                    event(name = "event_name") { }
                }
            }
        }

        // THEN
        assertEquals(
            "An event must be in a top-level route declaration. It cannot be a sub-route",
            failure.message,
        )
    }

    @Test
    fun shouldThrowExceptionWhenEventNameContainsSlash() = runTest {
        // WHEN
        val failure = assertFails {
            routing {
                event(name = "/event_name") { }
            }
        }

        // THEN
        assertEquals(
            "Event name '/event_name' cannot contains '/'",
            failure.message,
        )
    }
}
