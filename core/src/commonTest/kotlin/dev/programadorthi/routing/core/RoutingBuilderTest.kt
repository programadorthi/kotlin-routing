package dev.programadorthi.routing.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RoutingBuilderTest {

    @Test
    fun shouldNavigateByPath() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = "/path") {
                    handle {
                        result = "path-handled"
                        handled()
                    }
                }
            }

            routing.push(path = "/path")
        }

        assertEquals(result, "path-handled")
    }

    @Test
    fun shouldNavigateByName() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = "/path", name = "path") {
                    handle {
                        result = "name-handled"
                        handled()
                    }
                }
            }

            routing.pushNamed(name = "path")
        }

        assertEquals(result, "name-handled")
    }

    @Test
    fun shouldCreateARouteUsingHandleDirectly() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                handle(path = "/path", name = "path") {
                    result = "route-created"
                    handled()
                }
            }

            routing.pushNamed(name = "path")
        }

        assertEquals(result, "route-created")
    }

    @Test
    fun shouldCreateARouteUsingPushDirectly() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                push(path = "/path", name = "path") {
                    result = "route-created"
                    handled()
                }
            }

            routing.pushNamed(name = "path")
        }

        assertEquals(result, "route-created")
    }

    @Test
    fun shouldCreateARouteUsingReplaceDirectly() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                replace(path = "/path", name = "path") {
                    result = "route-created"
                    handled()
                }
            }

            routing.replaceNamed(name = "path")
        }

        assertEquals(result, "route-created")
    }

    @Test
    fun shouldHandlePushWhenPushingARoute() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = "/path", name = "path") {
                    push {
                        result = "pushed-route"
                        handled()
                    }
                    replace {
                        result = "replaced-route"
                    }
                }
            }

            routing.pushNamed(name = "path")
        }

        assertEquals(result, "pushed-route")
    }

    @Test
    fun shouldHandleReplaceWhenReplacingARoute() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = "/path", name = "path") {
                    push {
                        result = "pushed-route"
                    }
                    replace {
                        result = "replaced-route"
                        handled()
                    }
                }
            }

            routing.replaceNamed(name = "path")
        }

        assertEquals(result, "replaced-route")
    }

    @Test
    fun shouldHandlePopWhenPoppingARoute() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = "/path", name = "path") {
                    push {
                        result = "pushed-route"
                    }
                    replace {
                        result = "replaced-route"
                    }
                    pop {
                        result = "popped-route"
                        handled()
                    }
                }
            }

            // A previous route must exist to pop
            routing.pushNamed(name = "path")
            routing.pop()
        }

        assertEquals(result, "popped-route")
    }

    @Test
    fun shouldRedirectToOtherRouteByPath() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = "/path", name = "path") {
                    redirectToPath(path = "/path2")
                }

                route(path = "/path2", name = "path2") {
                    // Any redirections are transformed to a replace route
                    replace {
                        result = "redirected-route"
                        handled()
                    }
                }
            }

            routing.pushNamed(name = "path")
        }

        assertEquals(result, "redirected-route")
    }

    @Test
    fun shouldRedirectToOtherRouteByName() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = "/path", name = "path") {
                    redirectToName(name = "path2")
                }

                route(path = "/path2", name = "path2") {
                    // Any redirections are transformed to a replace route
                    replace {
                        result = "redirected-route"
                        handled()
                    }
                }
            }

            routing.pushNamed(name = "path")
        }

        assertEquals(result, "redirected-route")
    }

    private fun whenBody(
        body: CoroutineContext.(() -> Unit) -> Unit
    ) = runTest {
        val job = Job()
        (coroutineContext + job).body {
            job.cancel()
        }
        job.join()
    }
}
