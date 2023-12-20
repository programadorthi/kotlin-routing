package dev.programadorthi.routing.compose

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.route
import dev.programadorthi.routing.core.routing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class ComposeRoutingTest {

    @Test
    fun shouldInvokeInitialContentWhenThereIsNoEmittedComposable() =
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
            assertEquals("I'm the initial content", fakeContent.result)
        }

    @Test
    fun shouldComposeByPath() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()

            val routing = routing(parentCoroutineContext = coroutineContext) {
                composable(path = "/path") {
                    fakeContent.content = "I'm the path based content"
                    fakeContent.Composable()
                }
            }

            composition.setContent {
                Routing(
                    routing = routing,
                    initial = {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    },
                )
            }

            // WHEN
            routing.execute(
                ApplicationCall(
                    application = routing.application,
                    uri = "/path",
                )
            )
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("I'm the path based content", fakeContent.result)
        }

    @Test
    fun shouldComposeByName() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()

            val routing = routing(parentCoroutineContext = coroutineContext) {
                composable(path = "/path", name = "path") {
                    fakeContent.content = "I'm the name based content"
                    fakeContent.Composable()
                }
            }

            composition.setContent {
                Routing(
                    routing = routing,
                    initial = {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    },
                )
            }

            // WHEN
            routing.execute(
                ApplicationCall(
                    application = routing.application,
                    name = "path",
                )
            )
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("I'm the name based content", fakeContent.result)
        }

    @Test
    fun shouldComposeByCustomRouteMethod() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()

            val routing = routing(parentCoroutineContext = coroutineContext) {
                composable(path = "/path", method = RouteMethod.Empty) {
                    fakeContent.content = "I'm the route method based content"
                    fakeContent.Composable()
                }
            }

            composition.setContent {
                Routing(
                    routing = routing,
                    initial = {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    },
                )
            }

            // WHEN
            routing.execute(
                ApplicationCall(
                    application = routing.application,
                    uri = "/path",
                    routeMethod = RouteMethod.Empty,
                )
            )
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("I'm the route method based content", fakeContent.result)
        }

    @Test
    fun shouldComposeByAnyRoute() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()

            val routing = routing(parentCoroutineContext = coroutineContext) {
                route(path = "/any") {
                    composable {
                        fakeContent.content = "I'm the generic based content"
                        fakeContent.Composable()
                    }
                }
            }

            composition.setContent {
                Routing(
                    routing = routing,
                    initial = {
                        fakeContent.content = "I'm the initial content"
                        fakeContent.Composable()
                    },
                )
            }

            // WHEN
            routing.execute(
                ApplicationCall(
                    application = routing.application,
                    uri = "/any",
                )
            )
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals("I'm the generic based content", fakeContent.result)
        }
}
