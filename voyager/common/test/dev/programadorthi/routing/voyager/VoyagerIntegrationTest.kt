package dev.programadorthi.routing.voyager

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAll
import dev.programadorthi.routing.core.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNotNull

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
            assertEquals("No instance for key AttributeKey: VoyagerNavigatorManager", result.message)
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
        var result: TestScreen? = null

        executeBody { coroutineContext, composition ->
            // GIVEN
            val router = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerNavigator)

                screen(path = "/path") {
                    TestScreen(value = "push test").also {
                        result = it
                    }
                }
            }

            composition.setContent {
                VoyagerRouter(router = router)
            }

            // WHEN
            router.push(path = "/path")
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals("push test", result?.value)
        }
    }

    @Test
    fun shouldReplaceAScreen() {
        var event: RouteMethod? = null
        var result: TestScreen? = null

        executeBody { coroutineContext, composition ->
            // GIVEN
            val router = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerNavigator)

                screen(path = "/path") {
                    TestScreen(value = "push test")
                }

                screen(path = "/path2") {
                    TestScreen(value = "replace test").also {
                        result = it
                        event = call.routeMethod
                    }
                }
            }

            composition.setContent {
                VoyagerRouter(router = router)
            }

            // WHEN
            router.push(path = "/path")
            advanceTimeBy(99)
            router.replace(path = "/path2")
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals("replace test", result?.value)
            assertEquals(StackRouteMethod.Replace, event)
        }
    }

    @Test
    fun shouldReplaceAllAScreen() {
        var event: RouteMethod? = null
        var result: TestScreen? = null

        executeBody { coroutineContext, composition ->
            // GIVEN
            val router = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerNavigator)

                screen(path = "/path") {
                    TestScreen(value = "push test")
                }

                screen(path = "/path2") {
                    TestScreen(value = "replace all test").also {
                        result = it
                        event = call.routeMethod
                    }
                }
            }

            composition.setContent {
                VoyagerRouter(router = router)
            }

            // WHEN
            router.push(path = "/path")
            advanceTimeBy(99)
            router.replaceAll(path = "/path2")
            advanceTimeBy(99)

            // THEN
            assertNotNull(result)
            assertEquals("replace all test", result?.value)
            assertEquals(StackRouteMethod.ReplaceAll, event)
        }
    }

    @Test
    fun shouldPopAScreen() {
        var event: RouteMethod? = null
        val sequence = mutableListOf<String>()

        executeBody { coroutineContext, composition ->
            val job = Job(parent = coroutineContext[Job])
            // GIVEN
            val router = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerNavigator)

                screen(path = "/path") {
                    TestScreen(value = "pop test").also {
                        sequence += "pushed screen"
                        event = call.routeMethod
                    }
                }

                pop(path = "/path") {
                    sequence += "popped screen"
                    event = call.routeMethod
                    job.complete() // A hack to wait receive pop event from Hook
                }
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
            assertEquals(listOf("pushed screen", "popped screen"), sequence)
            assertEquals(StackRouteMethod.Pop, event)
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
