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
import kotlin.test.assertFails
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class EventResourcesRoutingTest {

    @Event("app_start")
    class AppStart

    @Event("invalid/")
    class Invalid

    @Event("screen_view")
    class ScreenView(val screenName: String)

    @Test
    fun shouldThrowExceptionHavingEventNameWithSlash() = runTest {
        // WHEN
        val failure = assertFails {
            routing {
                install(EventResources)

                event<Invalid> { }
            }
        }

        // THEN
        assertEquals(
            "Event name 'invalid/' cannot contains '/'",
            failure.message,
        )
    }

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
}
