package dev.programadorthi.routing.compose.history

import dev.programadorthi.routing.compose.Routing
import dev.programadorthi.routing.compose.composable
import dev.programadorthi.routing.compose.helper.runComposeTest
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.handle
import dev.programadorthi.routing.core.routing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeHistoryAttributeTest {
    @Test
    fun shouldUseMemoryHistoryMode() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing = routing(parentCoroutineContext = coroutineContext) {
                composable(path = "/initial") {
                }
            }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(ComposeHistoryMode.Memory, routing.historyMode)
        }

    @Test
    fun shouldUseHtml5HistoryMode() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing = routing(parentCoroutineContext = coroutineContext) {
                composable(path = "/initial") {
                }
            }

            // WHEN
            composition.setContent {
                Routing(
                    historyMode = ComposeHistoryMode.Html5,
                    routing = routing,
                    startUri = "/initial",
                )
            }
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(ComposeHistoryMode.Html5, routing.historyMode)
        }

    @Test
    fun shouldUseHashHistoryMode() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing = routing(parentCoroutineContext = coroutineContext) {
                composable(path = "/initial") {
                }
            }

            // WHEN
            composition.setContent {
                Routing(
                    historyMode = ComposeHistoryMode.Hash,
                    routing = routing,
                    startUri = "/initial",
                )
            }
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(ComposeHistoryMode.Hash, routing.historyMode)
        }

    @Test
    fun shouldCallBeNeglected() =
        runTest {
            // GIVEN
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + Job()) {
                    handle(path = "/test") {
                        result = call
                    }
                }

            // WHEN
            val call =
                ApplicationCall(
                    application = routing.application,
                    uri = "/test",
                )
            call.neglect = true
            routing.execute(call)
            advanceTimeBy(99)

            // THEN
            assertEquals(true, result?.neglect)
        }

    @Test
    fun shouldCallBeRestored() =
        runTest {
            // GIVEN
            var result: ApplicationCall? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + Job()) {
                    handle(path = "/test") {
                        result = call
                    }
                }

            // WHEN
            val call =
                ApplicationCall(
                    application = routing.application,
                    uri = "/test",
                )
            call.restored = true
            routing.execute(call)
            advanceTimeBy(99)

            // THEN
            assertEquals(true, result?.restored)
        }
}
