package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.call
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RoutingTest {

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
    fun shouldNavigateByPathWithParameters() {
        var result = ""
        var parameters = Parameters.Empty

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = "/path") {
                    handle {
                        result = "path-handled"
                        parameters = call.parameters
                        handled()
                    }
                }
            }

            routing.push(path = "/path", parameters = parametersOf("key", "value"))
        }

        assertEquals(result, "path-handled")
        assertEquals(parameters, parametersOf("key", "value"))
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
    fun shouldNavigateByNameWithParameters() {
        var result = ""
        var parameters = Parameters.Empty

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = "/path", name = "path") {
                    handle {
                        result = "name-handled"
                        parameters = call.parameters
                        handled()
                    }
                }
            }

            routing.pushNamed(name = "path", parameters = parametersOf("key", "value"))
        }

        assertEquals(result, "name-handled")
        assertEquals(parameters, parametersOf("key", "value"))
    }

    @Test
    fun shouldNavigateByNameWithPathParameters() {
        var result = ""
        var parameters = Parameters.Empty

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = "/path/{id}", name = "path") {
                    handle {
                        result = "name-handled"
                        parameters = call.parameters
                        handled()
                    }
                }
            }

            routing.pushNamed(
                name = "path",
                pathParameters = parametersOf("id", "123"),
            )
        }

        assertEquals(result, "name-handled")
        assertEquals(parameters, parametersOf("id", "123"))
    }

    @Test
    fun shouldPopWithParameters() {
        var result = ""
        var parameters = Parameters.Empty

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = "/path", name = "path") {
                    push { }
                    pop {
                        result = "popped"
                        parameters = call.parameters
                        handled()
                    }
                }
            }

            // A previous route must exist to pop
            routing.pushNamed(name = "path")
            routing.pop(parameters = parametersOf("key", "value"))
        }

        assertEquals(result, "popped")
        assertEquals(parameters, parametersOf("key", "value"))
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
    fun shouldCreateARouteUsingPopDirectly() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                push(path = "/path") { }
                pop(path = "/path") {
                    result = "popped-handled"
                    handled()
                }
            }

            // A previous route must exist to pop
            routing.push(path = "/path")
            routing.pop()
        }

        assertEquals(result, "popped-handled")
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

    @Test
    fun shouldRouteByRegex() {
        var result = ""

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = Regex("/(?<number>\\d+)")) {
                    push(path = "/regex") {
                        result = "pushed-to-regex"
                        handled()
                    }
                }
            }

            routing.push(path = "/123/regex")
        }

        assertEquals(result, "pushed-to-regex")
    }

    @Test
    fun shouldRouteByRegexWithParameters() {
        var result = ""
        var parameters = Parameters.Empty

        whenBody { handled ->
            val routing = routing(parentCoroutineContext = this) {
                route(path = Regex("/(?<number>\\d+)")) {
                    push(path = "/regex") {
                        result = "pushed-to-regex"
                        parameters = call.parameters
                        handled()
                    }
                }
            }

            routing.push(path = "/123/regex")
        }

        assertEquals(result, "pushed-to-regex")
        assertEquals(parameters, parametersOf("number", "123"))
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
