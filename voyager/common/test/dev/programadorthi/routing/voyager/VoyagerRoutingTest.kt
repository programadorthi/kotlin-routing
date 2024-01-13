package dev.programadorthi.routing.voyager

import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.createApplicationPlugin
import dev.programadorthi.routing.core.application.hooks.CallFailed
import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.voyager.helper.FakeScreen
import dev.programadorthi.routing.voyager.helper.runComposeTest
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
internal class VoyagerRoutingTest {

    @Test
    fun shouldInvokeInitialScreenWhenANavigatorIsCreated() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing = routing(parentCoroutineContext = coroutineContext) {}
            val fakeScreen = FakeScreen().apply {
                content = "Hey, I am the initial screen"
            }

            // WHEN
            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = fakeScreen,
                )
            }
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("Hey, I am the initial screen", fakeScreen.composed)
        }

    @Test
    fun shouldNavigateByPath() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeScreen = FakeScreen()

            val routing = routing(parentCoroutineContext = coroutineContext) {
                screen(path = "/path") {
                    fakeScreen.apply {
                        content = "Hey, I am the called screen"
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                )
            }

            // WHEN
            routing.call(uri = "/path", routeMethod = VoyagerRouteMethod.Push)
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("Hey, I am the called screen", fakeScreen.composed)
        }

    @Test
    fun shouldNavigateByName() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeScreen = FakeScreen()

            val routing = routing(parentCoroutineContext = coroutineContext) {
                screen(path = "/path", name = "path") {
                    fakeScreen.apply {
                        content = "Hey, I am the called screen"
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                )
            }

            // WHEN
            routing.call(name = "path", routeMethod = VoyagerRouteMethod.Push)
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("Hey, I am the called screen", fakeScreen.composed)
        }

    @Test
    fun shouldThrowAnExceptionWhenNavigatingUsingANoValidRouteMethod() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var result: ApplicationCall? = null
            var exception: Throwable? = null

            val statusPages = createApplicationPlugin("status-pages") {
                on(CallFailed) { call, cause ->
                    result = call
                    exception = cause
                }
            }

            val routing = routing(parentCoroutineContext = coroutineContext) {
                install(statusPages)

                screen(path = "/path", method = RouteMethod.Empty) {
                    FakeScreen()
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                )
            }

            // WHEN
            routing.call(uri = "/path", routeMethod = RouteMethod.Empty)
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(result)
            assertNotNull(exception)
            assertEquals("/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Empty, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
            assertIs<IllegalStateException>(exception)
            assertEquals(
                "Voyager needs a stack route method to work. You called a screen /path using " +
                    "route method RouteMethodImpl(value=EMPTY) that is not supported by Voyager",
                exception?.message
            )
        }

    @Test
    fun shouldNavigateByAnyRoute() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeScreen = FakeScreen()

            val routing = routing(parentCoroutineContext = coroutineContext) {
                route(path = "/any") {
                    screen {
                        fakeScreen.apply {
                            content = "Hey, I am the called screen"
                        }
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                )
            }

            // WHEN
            routing.call(uri = "/any", routeMethod = VoyagerRouteMethod.Push)
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("Hey, I am the called screen", fakeScreen.composed)
        }

    @Test
    fun shouldPushAScreen() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                screen(path = "/path") {
                    FakeScreen().apply {
                        content = "Hey, I am the pushed screen"
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val lastScreen = navigator?.lastItemOrNull as? FakeScreen

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
                screen(path = "/push") {
                    FakeScreen().apply {
                        content = "Hey, I am the pushed screen"
                    }
                }

                screen(path = "/replace") {
                    FakeScreen().apply {
                        content = "Hey, I am the replaced screen"
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val pushedScreen = navigator?.lastItemOrNull as? FakeScreen

            routing.replace(path = "/replace")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val replacedScreen = navigator?.lastItemOrNull as? FakeScreen

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
                screen(path = "/push") {
                    FakeScreen().apply {
                        content = "Hey, I am the pushed screen number ${counter++}"
                    }
                }

                screen(path = "/replace") {
                    FakeScreen().apply {
                        content = "Hey, I am the replaced all screen"
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val firstPushedScreen = navigator?.lastItemOrNull as? FakeScreen

            routing.push(path = "/push")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val secondPushedScreen = navigator?.lastItemOrNull as? FakeScreen

            routing.replaceAll(path = "/replace")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val replacedAllScreen = navigator?.lastItemOrNull as? FakeScreen

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
    fun shouldPopAScreen() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                screen(path = "/push1") {
                    FakeScreen().apply {
                        content = "Hey, I am the pushed screen number 1"
                    }
                }

                screen(path = "/push2") {
                    FakeScreen().apply {
                        content = "Hey, I am the pushed screen number 2"
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen().apply {
                        content = "I am the initial screen"
                    },
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(path = "/push1")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val firstPushedScreen = navigator?.lastItemOrNull as? FakeScreen

            routing.push(path = "/push2")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val secondPushedScreen = navigator?.lastItemOrNull as? FakeScreen

            routing.pop()
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val lastScreen = navigator?.lastItemOrNull as? FakeScreen

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
            assertEquals(
                firstPushedScreen?.key,
                lastScreen?.key,
                "After pop should call first pushed screen content"
            )
        }

    @Test
    fun shouldPopAScreenAndSendResultToPreviousScreen() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                screen(path = "/push1") {
                    FakeScreen().apply {
                        content = "Hey, I am the pushed screen number 1"
                    }
                }

                screen(path = "/push2") {
                    FakeScreen().apply {
                        content = "Hey, I am the pushed screen number 2"
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen().apply {
                        content = "I am the initial screen"
                    },
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(path = "/push1")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val firstPushedScreen = navigator?.lastItemOrNull as? FakeScreen

            routing.push(path = "/push2")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            routing.pop(parameters = parametersOf("key" to listOf("value")))
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(parametersOf("key" to listOf("value")), firstPushedScreen?.parameters)
        }

    @Test
    fun shouldPopUntilAScreenAndSendResultToPreviousScreen() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            var navigator: Navigator? = null

            val routing = routing(parentCoroutineContext = coroutineContext) {
                screen(path = "/push1") {
                    FakeScreen().apply {
                        content = "Hey, I am the pushed screen number 1"
                    }
                }

                screen(path = "/push2") {
                    FakeScreen().apply {
                        content = "Hey, I am the pushed screen number 2"
                    }
                }
            }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen().apply {
                        content = "I am the initial screen"
                    },
                ) { nav ->
                    navigator = nav
                    CurrentScreen()
                }
            }

            // WHEN
            routing.push(path = "/push1")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition
            val firstPushedScreen = navigator?.lastItemOrNull as? FakeScreen

            repeat(5) {
                routing.push(path = "/push2")
                advanceTimeBy(99) // Ask for routing
                clock.sendFrame(0L) // Ask for recomposition
            }

            routing.popUntil(
                parameters = parametersOf("key" to listOf("value")),
            ) { screen ->
                screen == firstPushedScreen
            }
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(parametersOf("key" to listOf("value")), firstPushedScreen?.parameters)
        }
}
