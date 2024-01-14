package dev.programadorthi.routing.compose

import dev.programadorthi.routing.compose.helper.FakeContent
import dev.programadorthi.routing.compose.helper.runComposeTest
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.StackRouting
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.resources.Resources
import dev.programadorthi.routing.resources.execute
import dev.programadorthi.routing.resources.push
import dev.programadorthi.routing.resources.replace
import dev.programadorthi.routing.resources.replaceAll
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.resources.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeResourcesTest {
    @Resource("/path")
    class Path {
        @Resource("{id}")
        class Id(val parent: Path = Path(), val id: Int)
    }

    @Test
    fun shouldExecuteByType() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()
            var result: ApplicationCall? = null
            var path: Path? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    install(Resources)

                    composable<Path>(method = RouteMethod.Empty) {
                        result = call
                        path = it
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
            routing.execute(Path())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(path)
            assertEquals("I'm the path based content", fakeContent.result)
            assertEquals("/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Empty, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldPushByType() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()
            var result: ApplicationCall? = null
            var path: Path? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    install(Resources)
                    install(StackRouting)

                    composable<Path>(method = RouteMethod.Push) {
                        result = call
                        path = it
                        fakeContent.content = "I'm the push based content"
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
            routing.push(Path())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(path)
            assertEquals("I'm the push based content", fakeContent.result)
            assertEquals("/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldReplaceByType() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()
            var result: ApplicationCall? = null
            var path: Path? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    install(Resources)
                    install(StackRouting)

                    composable<Path> {
                        result = call
                        path = it
                        fakeContent.content = "I'm the replace based content"
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
            routing.replace(Path())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(path)
            assertEquals("I'm the replace based content", fakeContent.result)
            assertEquals("/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Replace, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldReplaceAllByType() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()
            var result: ApplicationCall? = null
            var path: Path? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    install(Resources)
                    install(StackRouting)

                    composable<Path> {
                        result = call
                        path = it
                        fakeContent.content = "I'm the replace all based content"
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
            routing.replaceAll(Path())
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(path)
            assertEquals("I'm the replace all based content", fakeContent.result)
            assertEquals("/path", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.ReplaceAll, result?.routeMethod)
            assertEquals(Parameters.Empty, result?.parameters)
        }

    @Test
    fun shouldPushByTypeAndParameters() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()
            var result: ApplicationCall? = null
            var id: Path.Id? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    install(Resources)
                    install(StackRouting)

                    composable<Path.Id> {
                        result = call
                        id = it
                        fakeContent.content = "I'm the push based content"
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
            routing.push(Path.Id(id = 123))
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(result)
            assertEquals("I'm the push based content", fakeContent.result)
            assertEquals(123, id?.id)
            assertEquals("/path/123", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf("id", "123"), result?.parameters)
        }

    @Test
    fun shouldReplaceByTypeAndParameters() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()
            var result: ApplicationCall? = null
            var id: Path.Id? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    install(Resources)
                    install(StackRouting)

                    composable<Path.Id> {
                        result = call
                        id = it
                        fakeContent.content = "I'm the replace based content"
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
            routing.replace(Path.Id(id = 123))
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(result)
            assertEquals("I'm the replace based content", fakeContent.result)
            assertEquals(123, id?.id)
            assertEquals("/path/123", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Replace, result?.routeMethod)
            assertEquals(parametersOf("id", "123"), result?.parameters)
        }

    @Test
    fun shouldReplaceAllByTypeAndParameters() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()
            var result: ApplicationCall? = null
            var id: Path.Id? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    install(Resources)
                    install(StackRouting)

                    composable<Path.Id> {
                        result = call
                        id = it
                        fakeContent.content = "I'm the replace all based content"
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
            routing.replaceAll(Path.Id(id = 123))
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(result)
            assertEquals("I'm the replace all based content", fakeContent.result)
            assertEquals(123, id?.id)
            assertEquals("/path/123", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.ReplaceAll, result?.routeMethod)
            assertEquals(parametersOf("id", "123"), result?.parameters)
        }

    @Test
    fun shouldPopAPushedType() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()
            var result: ApplicationCall? = null
            val ids = mutableListOf<Path.Id>()

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    install(Resources)
                    install(StackRouting)

                    composable<Path.Id> {
                        result = call
                        ids += it
                        fakeContent.content = "I'm the reactive based content"
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
            routing.push(Path.Id(id = 1))
            advanceTimeBy(99)
            clock.sendFrame(0L)
            routing.push(Path.Id(id = 2))
            advanceTimeBy(99)
            clock.sendFrame(0L)
            routing.push(Path.Id(id = 3))
            advanceTimeBy(99)
            clock.sendFrame(0L)
            routing.pop()
            advanceTimeBy(99)
            clock.sendFrame(0L)
            routing.pop()
            advanceTimeBy(99)
            clock.sendFrame(0L)
            routing.pop()
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(result)
            assertEquals("I'm the reactive based content", fakeContent.result)
            assertEquals("/path/1", "${result?.uri}")
            assertEquals("", "${result?.name}")
            assertEquals(RouteMethod.Push, result?.routeMethod)
            assertEquals(parametersOf("id", listOf("1")), result?.parameters)
            // 1, 2, 3 are pushed
            // 3 is skipped and 2 is called to compose
            // 2 is skipped and 1 is called to compose
            assertEquals(listOf(1, 2, 3, 2, 1), ids.map { it.id })
        }

    @Test
    fun shouldHandleAnyStackAction() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val fakeContent = FakeContent()
            val result = mutableListOf<Pair<ApplicationCall, Path.Id>>()

            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    install(Resources)
                    install(StackRouting)

                    composable<Path.Id> {
                        result += call to it
                        fakeContent.content = "I'm the reactive based content"
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
            routing.push(Path.Id(id = 1))
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition
            routing.push(Path.Id(id = 2))
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition
            routing.replace(Path.Id(id = 3))
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition
            routing.replaceAll(Path.Id(id = 4))
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition
            routing.pop()
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertNotNull(result)
            assertEquals(RouteMethod.Push, result[0].first.routeMethod)
            assertEquals(1, result[0].second.id)
            assertEquals(RouteMethod.Push, result[1].first.routeMethod)
            assertEquals(2, result[1].second.id)
            assertEquals(RouteMethod.Replace, result[2].first.routeMethod)
            assertEquals(3, result[2].second.id)
            assertEquals(RouteMethod.ReplaceAll, result[3].first.routeMethod)
            assertEquals(4, result[3].second.id)
        }
}
