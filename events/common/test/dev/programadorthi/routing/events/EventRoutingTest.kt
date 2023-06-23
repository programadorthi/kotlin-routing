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
            "An event cannot be a child of other Route",
            failure.message,
        )
    }

    @Test
    fun shouldHandleAnEventWhenHavingSubRouting() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
            rootPath = "/parent",
            parentCoroutineContext = coroutineContext + job
        ) {
            event(name = "event_parent") {}
        }

        val routing = routing(
            rootPath = "/child",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            event(name = "event_child") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.emitEvent(name = "event_child")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("event_child", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(EventRouteMethod, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldHandleParentEventWhenCallingFromChild() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
            rootPath = "/parent",
            parentCoroutineContext = coroutineContext + job
        ) {
            event(name = "event_parent") {
                result = call
                job.complete()
            }
        }

        val routing = routing(
            rootPath = "/child",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            event(name = "event_child") {
            }
        }

        // WHEN
        routing.emitEvent(name = "event_parent")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("event_parent", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(EventRouteMethod, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldHandleChildEventWhenCallingFromParent() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val parent = routing(
            rootPath = "/parent",
            parentCoroutineContext = coroutineContext + job
        ) {
            event(name = "event_parent") {
            }
        }

        routing(
            rootPath = "/child",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            event(name = "event_child") {
                result = call
                job.complete()
            }
        }

        // WHEN
        parent.emitEvent(name = "/child/event_child")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/child/event_child", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(EventRouteMethod, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }
}
