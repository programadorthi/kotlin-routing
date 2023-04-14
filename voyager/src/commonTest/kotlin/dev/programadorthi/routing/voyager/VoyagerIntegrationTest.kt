package dev.programadorthi.routing.voyager

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VoyagerIntegrationTest {

    @Test
    fun shouldThrowAnExceptionWhenVoyagerNavigatorPluginIsNotInstalled() {
        executeBody { coroutineContext, composition ->
            // GIVEN
            val router = routing(parentCoroutineContext = coroutineContext) {}
            val result = assertFails {
                composition.setContent {
                    VoyagerRouter(router = router)
                }
            }
            // THEN
            assertIs<IllegalStateException>(result)
            assertEquals(result.message, "No instance for key AttributeKey: VoyagerEventManager")
        }
    }

    @Test
    fun shouldNotThrowAnExceptionWhenVoyagerNavigatorPluginIsInstalled() {
        executeBody { coroutineContext, composition ->
            // GIVEN
            val router = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerNavigator)
            }
            composition.setContent {
                VoyagerRouter(router = router)
            }
            // THEN
        }
    }

    @Test
    fun shouldPushAScreen() {
        executeBody { coroutineContext, composition ->
            // GIVEN
            val router = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerNavigator) {
                    initialUri("")
                }

                push(path = "/path") {
                    TestScreen(value = "push test")
                }
            }

            val events = mutableListOf<VoyagerRouteEvent>()
            backgroundScope.launch(UnconfinedTestDispatcher()) {
                router.application.voyagerEventManager.navigation.toList(events)
            }

            composition.setContent {
                VoyagerRouter(router = router)
            }

            // WHEN
            router.push(path = "/path")
            advanceTimeBy(99)

            // THEN
            assertEquals(events.size, 2)
            assertIs<VoyagerRouteEvent.Idle>(events.first())
            assertIs<VoyagerRouteEvent.Push>(events.last())
            val event = events.last() as VoyagerRouteEvent.Push
            assertIs<TestScreen>(event.screen)
            assertEquals(event.screen.value, "push test")
        }
    }

    @Test
    fun shouldReplaceAScreen() {
        executeBody { coroutineContext, composition ->
            // GIVEN
            val router = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerNavigator) {
                    initialUri("")
                }

                push(path = "/path") {
                    TestScreen(value = "push test")
                }

                replace(path = "/path") {
                    TestScreen(value = "replace test")
                }
            }

            val events = mutableListOf<VoyagerRouteEvent>()
            backgroundScope.launch(UnconfinedTestDispatcher()) {
                router.application.voyagerEventManager.navigation.toList(events)
            }

            composition.setContent {
                VoyagerRouter(router = router)
            }

            // WHEN
            router.push(path = "/path")
            advanceTimeBy(99)
            router.replace(path = "/path")
            advanceTimeBy(99)

            // THEN
            assertEquals(events.size, 3)
            assertIs<VoyagerRouteEvent.Idle>(events.first())
            assertIs<VoyagerRouteEvent.Push>(events[1])
            assertIs<VoyagerRouteEvent.Replace>(events.last())
            val event = events.last() as VoyagerRouteEvent.Replace
            assertIs<TestScreen>(event.screen)
            assertEquals(event.screen.value, "replace test")
            assertFalse { event.replaceAll }
        }
    }

    @Test
    fun shouldReplaceAllAScreen() {
        executeBody { coroutineContext, composition ->
            // GIVEN
            val router = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerNavigator) {
                    initialUri("")
                }

                push(path = "/path") {
                    TestScreen(value = "push test")
                }

                replaceAll(path = "/path") {
                    TestScreen(value = "replace all test")
                }
            }

            val events = mutableListOf<VoyagerRouteEvent>()
            backgroundScope.launch(UnconfinedTestDispatcher()) {
                router.application.voyagerEventManager.navigation.toList(events)
            }

            composition.setContent {
                VoyagerRouter(router = router)
            }

            // WHEN
            router.push(path = "/path")
            advanceTimeBy(99)
            router.replaceAll(path = "/path")
            advanceTimeBy(99)

            // THEN
            assertEquals(events.size, 3)
            assertIs<VoyagerRouteEvent.Idle>(events.first())
            assertIs<VoyagerRouteEvent.Push>(events[1])
            assertIs<VoyagerRouteEvent.Replace>(events.last())
            val event = events.last() as VoyagerRouteEvent.Replace
            assertIs<TestScreen>(event.screen)
            assertEquals(event.screen.value, "replace all test")
            assertTrue { event.replaceAll }
        }
    }

    @Test
    fun shouldPopAScreen() {
        executeBody { coroutineContext, composition ->
            val job = Job(parent = coroutineContext[Job])
            // GIVEN
            val router = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerNavigator) {
                    initialUri("")
                }

                push(path = "/path") {
                    TestScreen(value = "push test")
                }

                pop(path = "/path") {
                    job.complete() // A hack to wait receive pop event from Hook
                }
            }

            val events = mutableListOf<VoyagerRouteEvent>()
            backgroundScope.launch(UnconfinedTestDispatcher()) {
                router.application.voyagerEventManager.navigation.toList(events)
            }

            composition.setContent {
                VoyagerRouter(router = router)
            }

            // WHEN
            router.push(path = "/path")
            advanceTimeBy(99)
            router.pop()
            advanceTimeBy(99)

            // THEN
            assertEquals(events.size, 3)
            assertIs<VoyagerRouteEvent.Idle>(events.first())
            assertIs<VoyagerRouteEvent.Push>(events[1])
            assertIs<VoyagerRouteEvent.Pop>(events.last())
        }
    }

    private fun executeBody(
        body: TestScope.(CoroutineContext, Composition) -> Unit
    ) = runTest {
        // SETUP
        val job = Job()
        val clock = BroadcastFrameClock()
        val scope = CoroutineScope(coroutineContext + job + clock)
        val recomposer = Recomposer(scope.coroutineContext)
        val runner = scope.launch {
            recomposer.runRecomposeAndApplyChanges()
        }
        val composition = Composition(UnitApplier(), recomposer)
        try {
            body(scope.coroutineContext, composition)
        } finally {
            runner.cancel()
            recomposer.close()
            job.cancel()
        }
    }
}
