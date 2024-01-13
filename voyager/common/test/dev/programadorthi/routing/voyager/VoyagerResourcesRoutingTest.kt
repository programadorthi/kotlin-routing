package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import dev.programadorthi.routing.core.application.MissingApplicationPluginException
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.voyager.helper.FakeResourceScreen
import dev.programadorthi.routing.voyager.helper.Path
import dev.programadorthi.routing.voyager.helper.VoyagerResult
import dev.programadorthi.routing.voyager.helper.runComposeTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalCoroutinesApi::class)
internal class VoyagerResourcesRoutingTest {

    @Test
    fun shouldThrowExceptionWhenThePluginIsNotInstalled() {
        val exception = assertFails {
            routing {
                screen<FakeResourceScreen<VoyagerResult>>()
            }
        }
        assertIs<MissingApplicationPluginException>(exception)
        assertEquals("Application plugin VoyagerResources is not installed", exception.message)
    }

    @Test
    fun shouldPushAScreen() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerResources)

                screen<FakeResourceScreen<VoyagerResult>>()
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeResourceScreen<VoyagerResult>(),
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(
                resource = FakeResourceScreen<VoyagerResult>().apply {
                    content = "Hey, I am the pushed screen"
                }
            )
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val lastScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            // THEN
            assertEquals("Hey, I am the pushed screen", lastScreen?.composed)
            assertEquals(false, lastScreen?.disposed, "Last screen should not be disposed")
        }

    @Test
    fun shouldPushAScreenUsingOtherResource() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerResources)

                screen<Path> {
                    FakeResourceScreen<VoyagerResult>().apply {
                        content = "Hey, I am the pushed screen"
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeResourceScreen<VoyagerResult>(),
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(resource = Path())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val lastScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            // THEN
            assertEquals("Hey, I am the pushed screen", lastScreen?.composed)
            assertEquals(false, lastScreen?.disposed, "Last screen should not be disposed")
        }

    @Test
    fun shouldReplaceAScreen() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerResources)

                screen<FakeResourceScreen<VoyagerResult>>()
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeResourceScreen<VoyagerResult>(),
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(
                resource = FakeResourceScreen<VoyagerResult>().apply {
                    content = "Hey, I am the pushed screen"
                }
            )
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val pushedScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            routing.replace(
                resource = FakeResourceScreen<VoyagerResult>().apply {
                    content = "Hey, I am the replaced screen"
                }
            )
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val replacedScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            // THEN
            assertEquals("Hey, I am the pushed screen", pushedScreen?.composed)
            assertEquals(true, pushedScreen?.disposed, "First screen should be disposed")
            assertEquals("Hey, I am the replaced screen", replacedScreen?.composed)
            assertEquals(false, replacedScreen?.disposed, "Replaced screen should not be disposed")
        }

    @Test
    fun shouldReplaceAScreenUsingOtherResource() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerResources)

                screen<Path> {
                    FakeResourceScreen<VoyagerResult>().apply {
                        content = "Hey, I am the pushed screen"
                    }
                }

                screen<Path.Id> {
                    FakeResourceScreen<VoyagerResult>().apply {
                        content = "Hey, I am the replaced screen"
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeResourceScreen<VoyagerResult>(),
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(resource = Path())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val pushedScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            routing.replace(resource = Path.Id(id = 123))
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val replacedScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            // THEN
            assertEquals("Hey, I am the pushed screen", pushedScreen?.composed)
            assertEquals(true, pushedScreen?.disposed, "First screen should be disposed")
            assertEquals("Hey, I am the replaced screen", replacedScreen?.composed)
            assertEquals(false, replacedScreen?.disposed, "Replaced screen should not be disposed")
        }

    @Test
    fun shouldReplaceAllScreens() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var counter = 1
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerResources)

                screen<FakeResourceScreen<VoyagerResult>>()
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeResourceScreen<VoyagerResult>(),
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(
                resource = FakeResourceScreen<VoyagerResult>().apply {
                    content = "Hey, I am the pushed screen number ${counter++}"
                }
            )
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val firstPushedScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            routing.push(
                resource = FakeResourceScreen<VoyagerResult>().apply {
                    content = "Hey, I am the pushed screen number ${counter++}"
                }
            )
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val secondPushedScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            routing.replaceAll(
                resource = FakeResourceScreen<VoyagerResult>().apply {
                    content = "Hey, I am the replaced all screen"
                }
            )
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val replacedAllScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            // THEN
            assertEquals("Hey, I am the pushed screen number 1", firstPushedScreen?.composed)
            assertEquals(
                true,
                firstPushedScreen?.disposed,
                "First pushed screen should be disposed"
            )
            assertEquals("Hey, I am the pushed screen number 2", secondPushedScreen?.composed)
            assertEquals(
                true,
                secondPushedScreen?.disposed,
                "Second pushed screen should be disposed"
            )
            assertEquals("Hey, I am the replaced all screen", replacedAllScreen?.composed)
            assertEquals(
                false,
                replacedAllScreen?.disposed,
                "Replaced all screen should not be disposed"
            )
        }

    @Test
    fun shouldReplaceAllScreensUsingOtherResource() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var counter = 1
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerResources)

                screen<Path> {
                    FakeResourceScreen<VoyagerResult>().apply {
                        content = "Hey, I am the pushed screen number ${counter++}"
                    }
                }

                screen<Path.Id> {
                    FakeResourceScreen<VoyagerResult>().apply {
                        content = "Hey, I am the pushed screen number ${counter++}"
                    }
                }

                screen<Path.Name> {
                    FakeResourceScreen<VoyagerResult>().apply {
                        content = "Hey, I am the replaced all screen"
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeResourceScreen<VoyagerResult>(),
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(resource = Path())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val firstPushedScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            routing.push(resource = Path.Id(id = 123))
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val secondPushedScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            routing.replaceAll(resource = Path.Name())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val replacedAllScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            // THEN
            assertEquals("Hey, I am the pushed screen number 1", firstPushedScreen?.composed)
            assertEquals(
                true,
                firstPushedScreen?.disposed,
                "First pushed screen should be disposed"
            )
            assertEquals("Hey, I am the pushed screen number 2", secondPushedScreen?.composed)
            assertEquals(
                true,
                secondPushedScreen?.disposed,
                "Second pushed screen should be disposed"
            )
            assertEquals("Hey, I am the replaced all screen", replacedAllScreen?.composed)
            assertEquals(
                false,
                replacedAllScreen?.disposed,
                "Replaced all screen should not be disposed"
            )
        }

    @Test
    fun shouldPopAScreenAndSendResultToPreviousScreen() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerResources)

                screen<FakeResourceScreen<String>>()
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeResourceScreen<VoyagerResult>().apply {
                        content = "I am the initial screen"
                    },
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(resource = FakeResourceScreen<String>())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val firstPushedScreen = navigator?.lastItemOrNull as? FakeResourceScreen<String>

            routing.push(resource = FakeResourceScreen<String>())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.pop(result = "This is the result provided by pop call")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("This is the result provided by pop call", firstPushedScreen?.parameters)
        }

    @Test
    fun shouldPopAScreenAndSendACustomSerializableResultToPreviousScreen() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                install(VoyagerResources)

                screen<FakeResourceScreen<VoyagerResult>>()
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeResourceScreen<VoyagerResult>().apply {
                        content = "I am the initial screen"
                    },
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(resource = FakeResourceScreen<VoyagerResult>())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val firstPushedScreen = navigator?.lastItemOrNull as? FakeResourceScreen<VoyagerResult>

            routing.push(resource = FakeResourceScreen<VoyagerResult>())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.pop(result = VoyagerResult("This is the result provided by pop call"))
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(
                VoyagerResult("This is the result provided by pop call"),
                firstPushedScreen?.parameters
            )
        }
}
