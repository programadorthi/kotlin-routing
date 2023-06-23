package dev.programadorthi.routing.events

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.events.resources.Event
import dev.programadorthi.routing.events.resources.EventResources
import dev.programadorthi.routing.events.resources.emitEvent
import dev.programadorthi.routing.events.resources.event
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class EventResourcesRoutingTest {

    @Event("app_start")
    class AppStart

    @Event("screen_view")
    class ScreenView(val screenName: String)

    @Event("event_parent")
    class EventParent

    @Event("event_child")
    class EventChild

    @Test
    fun shouldHandleByTypeAndEmitByName() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(EventResources)

            event<AppStart> {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.emitEvent(name = "app_start")
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("app_start", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(EventRouteMethod, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldHandleAndEmitByType() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(EventResources)

            event<AppStart> {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.emitEvent(AppStart())
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("app_start", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(EventRouteMethod, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
    }

    @Test
    fun shouldHandleByTypeWithParameters() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var screenView: ScreenView? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(EventResources)

            event<ScreenView> {
                screenView = it
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.emitEvent(ScreenView(screenName = "screen01"))
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertNotNull(screenView)
        assertEquals("screen_view", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(EventRouteMethod, result?.routeMethod)
        assertEquals(parametersOf("screenName", "screen01"), result?.parameters)
        assertEquals("screen01", screenView?.screenName)
    }

    @Test
    fun shouldHandleByNameAndParametersToType() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var screenView: ScreenView? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(EventResources)

            event<ScreenView> {
                screenView = it
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.emitEvent(name = "screen_view", parameters = parametersOf("screenName", "screen01"))
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertNotNull(screenView)
        assertEquals("screen_view", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(EventRouteMethod, result?.routeMethod)
        assertEquals(parametersOf("screenName", "screen01"), result?.parameters)
        assertEquals("screen01", screenView?.screenName)
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
            install(EventResources)

            event<EventParent> {}
        }

        val routing = routing(
            rootPath = "/child",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            install(EventResources)

            event<EventChild> {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.emitEvent(EventChild())
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
            install(EventResources)

            event<EventParent> {
                result = call
                job.complete()
            }
        }

        val routing = routing(
            rootPath = "/child",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            install(EventResources)

            event<EventChild> {
            }
        }

        // WHEN
        routing.emitEvent(EventParent())
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
            install(EventResources)

            event<EventParent> {}
        }

        routing(
            rootPath = "/child",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            install(EventResources)

            event<EventChild> {
                result = call
                job.complete()
            }
        }

        // WHEN
        // TODO: For now, typed event is not supported by parent looking for child. We need to use path based
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
