package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.generated.configure
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.util.Attributes
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class RoutingByAnnotationsTest {

    @Test
    fun shouldHandleAPath() =
        runTest {
            // GIVEN
            val path = "/path"
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = path)
            advanceTimeBy(99)

            // THEN
            assertEquals(emptyList<Any?>(), invoked.remove(path))
        }

    @Test
    fun shouldHandleAPathWithDoubleId() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/path/3.4")
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf(3.4), invoked.remove("/path/{id}"))
        }

    @Test
    fun shouldHandleByName() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(name = "named", parameters = parametersOf("name", "Routing"))
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf("Routing"), invoked.remove("/named/{name}"))
        }

    @Test
    fun shouldHandleByCustomPathName() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            val nextInt = Random.Default.nextInt()
            routing.call(uri = "/custom/$nextInt")
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf("$nextInt"), invoked.remove("/custom/{random}"))
        }

    @Test
    fun shouldHandleNullOptionalValue() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/optional")
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf(null), invoked.remove("/optional/{id?}"))
        }

    @Test
    fun shouldHandleNonNullOptionalValue() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/optional/ABC")
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf('A'), invoked.remove("/optional/{id?}"))
        }

    @Test
    fun shouldHandleTailcardOneParameter() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/tailcard/p1")
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf("p1"), invoked.remove("/tailcard/{param...}"))
        }

    @Test
    fun shouldHandleTailcardManyParameter() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/tailcard/p1/p2/p3/p4")
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf("p1", "p2", "p3", "p4"), invoked.remove("/tailcard/{param...}"))
        }

    @Test
    fun shouldHandleByRegex() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/foo/hello")
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf(), invoked.remove(".+/hello"))
        }

    @Test
    fun shouldHandleByRegexWithParameters() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/456")
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf(456), invoked.remove("/(?<number>\\d+)"))
        }

    @Test
    fun shouldHandleWithBody() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            val body = User(id = 456, name = "With Body")
            routing.callWithBody(uri = "/with-body", body = body)
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf(body), invoked.remove("/with-body"))
        }

    @Test
    fun shouldHandleWithNullBody() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/with-null-body")
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf(null), invoked.remove("/with-null-body"))
        }

    @Test
    fun shouldHandleWithNonNullBody() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            val body = User(id = 456, name = "With Body")
            routing.callWithBody(uri = "/with-null-body", body = body)
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf(body), invoked.remove("/with-null-body"))
        }

    @Test
    fun shouldHandleCustomMethod() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/path", routeMethod = RouteMethod.Push)
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf("PUSH"), invoked.remove("/path"))
        }

    @Test
    fun shouldHandleMultipleParameters() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/path/13579/partition")
            advanceTimeBy(99)

            // THEN
            assertEquals(listOf(13579, "partition"), invoked.remove("/path/{part1}/{part2}"))
        }

    @Test
    fun shouldHandleCallDirectly() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/call")
            advanceTimeBy(99)

            // THEN
            val call = invoked.remove("/call")?.firstOrNull() as? ApplicationCall
            assertNotNull(call)
            assertEquals("/call", call.uri)
            assertEquals("", call.name)
            assertEquals(RouteMethod.Empty, call.routeMethod)
            assertEquals(Parameters.Empty, call.parameters)
        }

    @Test
    fun shouldHandleCallProperties() =
        runTest {
            // GIVEN
            val job = Job()
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    configure()
                }

            // WHEN
            routing.call(uri = "/call/p01/p02")
            advanceTimeBy(99)

            // THEN
            val items = invoked.remove("/call/{part1}/{part2}")
            assertNotNull(items)
            assertIs<Application>(items.firstOrNull())
            assertEquals(parametersOf("part1" to listOf("p01"), "part2" to listOf("p02")), items[1])
            assertIs<Attributes>(items[2])
        }
}
