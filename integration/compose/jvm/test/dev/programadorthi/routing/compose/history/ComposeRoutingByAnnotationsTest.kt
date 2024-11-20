package dev.programadorthi.routing.compose.history

import dev.programadorthi.routing.compose.Routing
import dev.programadorthi.routing.compose.User
import dev.programadorthi.routing.compose.helper.runComposeTest
import dev.programadorthi.routing.compose.invoked
import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.pushNamed
import dev.programadorthi.routing.core.pushWithBody
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.generated.configure
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.util.Attributes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeRoutingByAnnotationsTest {

    @Test
    fun shouldHandleInitial() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(emptyList<Any?>(), invoked.remove("/initial"))
        }

    @Test
    fun shouldHandleAPath() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf(RouteMethod.Push.value), invoked.remove("/path"))
        }

    @Test
    fun shouldHandleAPathWithDoubleId() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/path/3.4")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf(3.4), invoked.remove("/path/{id}"))
        }

    @Test
    fun shouldHandleByName() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.pushNamed(name = "named", parameters = parametersOf("name", "Routing"))
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf("Routing"), invoked.remove("/named/{name}"))
        }

    @Test
    fun shouldHandleByCustomPathName() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            val nextInt = Random.Default.nextInt()
            routing.push(path = "/custom/$nextInt")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf("$nextInt"), invoked.remove("/custom/{random}"))
        }

    @Test
    fun shouldHandleNullOptionalValue() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/optional")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf(null), invoked.remove("/optional/{id?}"))
        }

    @Test
    fun shouldHandleNonNullOptionalValue() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/optional/ABC")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf('A'), invoked.remove("/optional/{id?}"))
        }

    @Test
    fun shouldHandleTailcardOneParameter() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/tailcard/p1")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf("p1"), invoked.remove("/tailcard/{param...}"))
        }

    @Test
    fun shouldHandleTailcardManyParameter() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/tailcard/p1/p2/p3/p4")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf("p1", "p2", "p3", "p4"), invoked.remove("/tailcard/{param...}"))
        }

    @Ignore("Compose Regex is not supported yet")
    @Test
    fun shouldHandleByRegex() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/foo/hello")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf(), invoked.remove(".+/hello"))
        }

    @Ignore("Compose Regex is not supported yet")
    @Test
    fun shouldHandleByRegexWithParameters() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/456")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf(456), invoked.remove("/(?<number>\\d+)"))
        }

    @Test
    fun shouldHandleWithBody() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            val body = User(id = 456, name = "With Body")
            routing.pushWithBody(path = "/with-body", body = body)
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf(body), invoked.remove("/with-body"))
        }

    @Test
    fun shouldHandleWithNullBody() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/with-null-body")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf(null), invoked.remove("/with-null-body"))
        }

    @Test
    fun shouldHandleWithNonNullBody() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            val body = User(id = 456, name = "With Body")
            routing.pushWithBody(path = "/with-null-body", body = body)
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf(body), invoked.remove("/with-null-body"))
        }

    @Test
    fun shouldHandleCustomMethod() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/path")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf("PUSH"), invoked.remove("/path"))
        }

    @Test
    fun shouldHandleMultipleParameters() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/path/13579/partition")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf(13579, "partition"), invoked.remove("/path/{part1}/{part2}"))
        }

    @Test
    fun shouldHandleCallDirectly() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/call")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            val call = invoked.remove("/call")?.firstOrNull() as? ApplicationCall
            assertNotNull(call)
            assertEquals("/call", call.uri)
            assertEquals("", call.name)
            assertEquals(RouteMethod.Push, call.routeMethod)
            assertEquals(Parameters.Empty, call.parameters)
        }

    @Test
    fun shouldHandleCallProperties() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            // WHEN
            composition.setContent {
                Routing(
                    routing = routing,
                    startUri = "/initial",
                )
            }
            advanceTimeBy(99)
            clock.sendFrame(0L) // Ask for recomposition

            routing.push(path = "/call/p01/p02")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            val items = invoked.remove("/call/{part1}/{part2}")
            assertNotNull(items)
            assertIs<Application>(items.firstOrNull())
            assertEquals(parametersOf("part1" to listOf("p01"), "part2" to listOf("p02")), items[1])
            assertIs<Attributes>(items[2])
        }
}
