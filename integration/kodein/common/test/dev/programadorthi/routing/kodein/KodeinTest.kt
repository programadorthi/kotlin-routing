package dev.programadorthi.routing.kodein

import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.handle
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.kodein.ext.instance
import dev.programadorthi.routing.statuspages.StatusPages
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.with
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class KodeinTest {
    data class Item(val id: Int)

    @Test
    fun shouldThrowExceptionWhenPluginIsNotInstalled() =
        runTest {
            // GIVEN
            val job = Job()
            var exception: Throwable? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(StatusPages) {
                        exception<Throwable> { _, cause ->
                            exception = cause
                        }
                    }

                    handle(path = "/path") {
                        val text: String by instance()
                        val item: Item by instance()
                        // Printing variables that ask to Kodein retrieve the instances
                        println("text: $text and item: $item")
                        job.complete()
                    }
                }

            // WHEN
            routing.call(uri = "/path")
            advanceTimeBy(99)

            // THEN
            assertIs<IllegalStateException>(exception)
            assertEquals("No DI container found for [/]", exception?.message)
        }

    @Test
    fun shouldGetInstancesFromDI() =
        runTest {
            // GIVEN
            val job = Job()
            var result: String? = null
            var item: Item? = null

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(DIPlugin) {
                        constant("test") with "kotlin routing"

                        bindProvider {
                            Item(id = 123)
                        }
                    }

                    handle(path = "/path") {
                        val text: String by closestDI().instance(tag = "test")
                        result = text
                        item = closestDI().direct.instance()
                        job.complete()
                    }
                }

            // WHEN
            routing.call(uri = "/path")
            advanceTimeBy(99)

            // THEN
            assertEquals("kotlin routing", result)
            assertEquals(Item(id = 123), item)
        }

    @Test
    fun shouldChildRoutingGetInstancesFromParent() =
        runTest {
            // GIVEN
            val job = Job()
            var result: String? = null
            var item: Item? = null

            val parent =
                routing {
                    install(DIPlugin) {
                        constant("test") with "kotlin routing"

                        bindProvider {
                            Item(id = 123)
                        }
                    }
                }

            val routing =
                routing(
                    parent = parent,
                    rootPath = "/child",
                    parentCoroutineContext = coroutineContext + job,
                ) {
                    handle(path = "/path") {
                        val text: String by instance(tag = "test")
                        result = text
                        item = closestDI().direct.instance()
                        job.complete()
                    }
                }

            // WHEN
            routing.call(uri = "/path")
            advanceTimeBy(99)

            // THEN
            assertEquals("kotlin routing", result)
            assertEquals(Item(id = 123), item)
        }

    @Test
    fun shouldChildRoutingHaveItOwnDI() =
        runTest {
            // GIVEN
            val job = Job()
            var result: String? = null
            var item: Item? = null

            val parent = routing { }

            val routing =
                routing(
                    parent = parent,
                    rootPath = "/child",
                    parentCoroutineContext = coroutineContext + job,
                ) {
                    install(DIPlugin) {
                        constant("test") with "kotlin routing"

                        bindProvider {
                            Item(id = 123)
                        }
                    }

                    handle(path = "/path") {
                        val text: String by instance(tag = "test")
                        result = text
                        item = closestDI().direct.instance()
                        job.complete()
                    }
                }

            // WHEN
            routing.call(uri = "/path")
            advanceTimeBy(99)

            // THEN
            assertEquals("kotlin routing", result)
            assertEquals(Item(id = 123), item)
        }

    @Test
    fun shouldChildRoutingExtendParentDIAndHaveASubDI() =
        runTest {
            // GIVEN
            val job = Job()
            var result: String? = null
            var item: Item? = null

            val parent =
                routing {
                    install(DIPlugin) {
                        constant("test") with "kotlin routing"
                    }
                }

            val routing =
                routing(
                    parent = parent,
                    rootPath = "/child",
                    parentCoroutineContext = coroutineContext + job,
                ) {
                    subDI {
                        bindProvider {
                            Item(id = 123)
                        }
                    }

                    handle(path = "/path") {
                        val text: String by instance(tag = "test")
                        result = text
                        item = closestDI().direct.instance()
                        job.complete()
                    }
                }

            // WHEN
            routing.call(uri = "/path")
            advanceTimeBy(99)

            // THEN
            assertEquals("kotlin routing", result)
            assertEquals(Item(id = 123), item)
        }

    @Test
    fun shouldParentRoutingNotGetAnInstanceFromChildRouting() =
        runTest {
            // GIVEN
            val job = Job()
            var exception: Throwable? = null

            val parent =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(StatusPages) {
                        exception<Throwable> { _, cause ->
                            exception = cause
                        }
                    }

                    install(DIPlugin) {
                        constant("test") with "kotlin routing"
                    }

                    handle(path = "/path") {
                        val text: String by instance(tag = "test")
                        val item: Item by instance()
                        // Printing variables that ask to Kodein retrieve the instances
                        println("text: $text and item: $item")
                        job.complete()
                    }
                }

            val routing =
                routing(
                    parent = parent,
                    rootPath = "/child",
                    parentCoroutineContext = coroutineContext + job,
                ) {
                    subDI {
                        bindProvider {
                            Item(id = 123)
                        }
                    }
                }

            // WHEN
            routing.call(uri = "/path")
            advanceTimeBy(99)

            // THEN
            assertIs<DI.NotFoundException>(exception)
            assertEquals(
                """No binding found for KodeinTest.Item""",
                exception?.message,
            )
        }

    @Test
    fun shouldExtendAnExistentDI() =
        runTest {
            // GIVEN
            val job = Job()
            var result: String? = null
            var item: Item? = null

            val externalDI =
                DI.lazy {
                    constant("test") with "kotlin routing"

                    bindProvider {
                        Item(id = 123)
                    }
                }

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(DIPlugin) {
                        extend(externalDI)
                    }

                    handle(path = "/path") {
                        val text: String by instance(tag = "test")
                        val aux: Item by instance()
                        result = text
                        item = aux
                        job.complete()
                    }
                }

            // WHEN
            routing.call(uri = "/path")
            advanceTimeBy(99)

            // THEN
            assertEquals("kotlin routing", result)
            assertEquals(Item(id = 123), item)
        }
}
