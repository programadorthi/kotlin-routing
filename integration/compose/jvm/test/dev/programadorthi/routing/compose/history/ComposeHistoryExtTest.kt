package dev.programadorthi.routing.compose.history

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import dev.programadorthi.routing.compose.Routing
import dev.programadorthi.routing.compose.callStack
import dev.programadorthi.routing.compose.composable
import dev.programadorthi.routing.compose.helper.FakeContent
import dev.programadorthi.routing.compose.helper.runComposeTest
import dev.programadorthi.routing.compose.push
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.handle
import dev.programadorthi.routing.core.routing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeHistoryExtTest {
    @Test
    fun shouldCallStackStartsWithInitialOnly() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing = routing(parentCoroutineContext = coroutineContext) {}
            val fakeContent = FakeContent()

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    initial = {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    },
                )
            }
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(1, routing.callStack.size)
        }

    @Test
    fun shouldUseMemoryHistoryMode() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing = routing(parentCoroutineContext = coroutineContext) {}
            val fakeContent = FakeContent()
            val stateRegistry =
                SaveableStateRegistry(
                    restoredValues = null,
                    canBeSaved = { true },
                )

            // WHEN
            composition.setContent {
                CompositionLocalProvider(LocalSaveableStateRegistry provides stateRegistry) {
                    Routing(
                        routing = routing,
                        initial = {
                            fakeContent.content = "I'm the initial content"
                            fakeContent.Composable()
                        },
                    )
                }
            }
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(1, routing.callStack.size)
        }

    @Test
    fun shouldCallStackStartsWithRestoreValues() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    handle(path = "/path3") {
                    }
                }
            val fakeContent = FakeContent()
            val history =
                listOf(
                    ApplicationCall(
                        application = routing.application,
                        uri = "/path1",
                    ).toHistoryState(),
                    ApplicationCall(
                        application = routing.application,
                        uri = "/path2",
                    ).toHistoryState(),
                    ApplicationCall(
                        application = routing.application,
                        uri = "/path3",
                    ).toHistoryState(),
                )
            val json = Json.encodeToString(history)
            val stateRegistry =
                SaveableStateRegistry(
                    restoredValues = mapOf(routing.toString() to listOf(json)),
                    canBeSaved = { true },
                )

            // WHEN
            composition.setContent {
                CompositionLocalProvider(LocalSaveableStateRegistry provides stateRegistry) {
                    Routing(
                        routing = routing,
                        initial = {
                            fakeContent.content = "I'm the initial content"
                            fakeContent.Composable()
                        },
                    )
                }
            }
            clock.sendFrame(0L) // Ask for recomposition
            advanceTimeBy(99)

            // THEN
            assertEquals(3, routing.callStack.size)
        }

    @Test
    fun shouldCallStackBeSaved() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    composable(path = "/path1") {
                    }
                    composable(path = "/path2") {
                    }
                    composable(path = "/path3") {
                    }
                }
            val fakeContent = FakeContent()
            val calls =
                listOf(
                    ApplicationCall(
                        application = routing.application,
                        uri = routing.toString(),
                    ),
                    ApplicationCall(
                        application = routing.application,
                        uri = "/path1",
                        routeMethod = RouteMethod.Push,
                    ),
                    ApplicationCall(
                        application = routing.application,
                        uri = "/path2",
                        routeMethod = RouteMethod.Push,
                    ),
                    ApplicationCall(
                        application = routing.application,
                        uri = "/path3",
                        routeMethod = RouteMethod.Push,
                    ),
                )
            val json = Json.encodeToString(calls.map { it.toHistoryState() })
            val expected = mapOf(routing.toString() to listOf(json))
            val stateRegistry =
                SaveableStateRegistry(
                    restoredValues = null,
                    canBeSaved = { true },
                )

            // WHEN
            composition.setContent {
                CompositionLocalProvider(LocalSaveableStateRegistry provides stateRegistry) {
                    Routing(
                        routing = routing,
                        initial = {
                            fakeContent.content = "I'm the initial content"
                            fakeContent.Composable()
                        },
                    )
                }
            }
            clock.sendFrame(0L) // Ask for recomposition
            advanceTimeBy(99)

            routing.push(path = "/path1")
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition
            routing.push(path = "/path2")
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition
            routing.push(path = "/path3")
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(calls.size, routing.callStack.size)
            assertEquals(expected, stateRegistry.performSave())
        }
}
