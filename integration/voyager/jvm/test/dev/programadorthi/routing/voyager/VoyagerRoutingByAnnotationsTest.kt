package dev.programadorthi.routing.voyager

import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.pushWithBody
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.generated.configure
import dev.programadorthi.routing.voyager.helper.FakeScreen
import dev.programadorthi.routing.voyager.helper.runComposeTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy

@OptIn(ExperimentalCoroutinesApi::class)
internal class VoyagerRoutingByAnnotationsTest {

    @Test
    fun shouldHandleScreenClass() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                )
            }

            // WHEN
            routing.push("/screen")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(emptyList(), invoked.remove("/screen"))
        }

    @Test
    fun shouldHandleScreenObject() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                )
            }

            // WHEN
            routing.push("/screen-object")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(emptyList(), invoked.remove("/screen-object"))
        }

    @Test
    fun shouldHandleScreenWithParameter() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                )
            }

            // WHEN
            val nextInt = Random.Default.nextInt()
            routing.push("/screen/$nextInt")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf(nextInt), invoked.remove("/screen/{id}"))
        }

    @Test
    fun shouldHandleScreenWithMultipleConstructors() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                )
            }

            // WHEN
            routing.push("/screen-with-name/Routing")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            val fromName = invoked.remove("/screen-with-name/{name}")

            routing.push("/screen-with-age/18")
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            val fromAge = invoked.remove("/screen-with-age/{age}")

            // THEN
            assertEquals(listOf("Routing"), fromName)
            assertEquals(listOf(18), fromAge)
        }

    @Test
    fun shouldHandleScreenWithBody() =
        runComposeTest { coroutineContext, composition, clock ->
            // GIVEN
            val routing =
                routing(parentCoroutineContext = coroutineContext) {
                    configure()
                }

            composition.setContent {
                VoyagerRouting(
                    routing = routing,
                    initialScreen = FakeScreen(),
                )
            }

            // WHEN
            val body = User(id = 456, name = "With Body")
            routing.pushWithBody(path = "/screen-with-body", body = body)
            advanceTimeBy(99) // Ask for routing
            clock.sendFrame(0L) // Ask for recomposition

            // THEN
            assertEquals(listOf(body), invoked.remove("/screen-with-body"))
        }

}
