@file:Suppress("TYPEALIAS_EXPANSION_DEPRECATION")

package dev.programadorthi.routing.koin

import dev.programadorthi.routing.core.application.application
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.handle
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.koin.ext.get
import dev.programadorthi.routing.koin.ext.getKoin
import dev.programadorthi.routing.koin.ext.inject
import dev.programadorthi.routing.statuspages.StatusPages
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.error.NoDefinitionFoundException
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class KoinTest {
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
                        val text: String by call.inject()
                        val item: Item by call.inject()
                        // Printing variables that ask to Koin retrieve the instances
                        println("text: $text and item: $item")
                        job.complete()
                    }
                }

            // WHEN
            routing.call(uri = "/path")
            advanceTimeBy(99)

            // THEN
            assertIs<IllegalStateException>(exception)
            assertEquals(
                "No Koin instance started. Use install(Koin) or startKoin()",
                exception?.message,
            )
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
                    install(Koin) {
                        setup {
                            modules(
                                module {
                                    factory {
                                        "kotlin routing"
                                    }

                                    factory {
                                        Item(id = 123)
                                    }
                                },
                            )
                        }
                    }

                    handle(path = "/path") {
                        val text: String by application.inject()
                        result = text
                        item = application.get()
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
                    install(Koin) {
                        setup {
                            modules(
                                module {
                                    factory(named("test")) {
                                        "kotlin routing"
                                    }

                                    factory {
                                        Item(id = 123)
                                    }
                                },
                            )
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
                        val text: String by inject(named("test"))
                        result = text
                        item = get()
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
                    install(Koin) {
                        setup {
                            modules(
                                module {
                                    factory {
                                        "kotlin routing"
                                    }

                                    factory {
                                        Item(id = 123)
                                    }
                                },
                            )
                        }
                    }

                    handle(path = "/path") {
                        val text: String by inject()
                        result = text
                        item = get()
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
                    install(Koin) {
                        setup {
                            modules(
                                module {
                                    factory {
                                        "kotlin routing"
                                    }
                                },
                            )
                        }
                    }
                }

            val routing =
                routing(
                    parent = parent,
                    rootPath = "/child",
                    parentCoroutineContext = coroutineContext + job,
                ) {
                    getKoin().loadModules(
                        modules =
                            listOf(
                                module {
                                    factory {
                                        Item(id = 123)
                                    }
                                },
                            ),
                    )

                    handle(path = "/path") {
                        val text: String by inject()
                        result = text
                        item = get()
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

                    install(Koin) {
                        setup {
                            modules(
                                module {
                                    factory {
                                        "kotlin routing"
                                    }
                                },
                            )
                        }
                    }

                    handle(path = "/path") {
                        val text: String by inject()
                        val item: Item = get()
                        // Printing variables that ask to Koin retrieve the instances
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
                    install(KoinIsolated) {
                        setup {
                            modules(
                                module {
                                    factory {
                                        Item(id = 123)
                                    }
                                },
                            )
                        }
                    }
                }

            // WHEN
            routing.call(uri = "/path")
            advanceTimeBy(99)

            // THEN
            val expect =
                "No definition found for type " +
                    "'dev.programadorthi.routing.koin.KoinTest${'$'}Item'. Check your Modules " +
                    "configuration and add missing type and/or qualifier!"
            assertIs<NoDefinitionFoundException>(exception)
            assertEquals(expect, exception?.message)
        }

    @Test
    fun shouldExtendAnExistentDI() =
        runTest {
            // GIVEN
            val job = Job()
            var result: String? = null
            var item: Item? = null

            val externalDI =
                startKoin {
                    modules(
                        module {
                            factory {
                                "kotlin routing"
                            }

                            factory {
                                Item(id = 123)
                            }
                        },
                    )
                }

            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(KoinIsolated) {
                        setup(koinApplication = externalDI) {}
                    }

                    handle(path = "/path") {
                        val text: String by inject()
                        val aux: Item = get()
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
