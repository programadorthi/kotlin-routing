package dev.programadorthi.routing.sessions

import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.redirectToPath
import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.handle
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTest {

    @Test
    fun shouldHaveASessionWhenLoggedIn() = runTest {
        val job = Job()
        var userSession: UserSession? = null

        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Sessions) {
                session<UserSession>()
            }

            handle(path = "/login") {
                call.sessions.set(UserSession(id = "123abc", count = 0))
                call.redirectToPath(path = "/user")
            }

            handle(path = "/user") {
                userSession = call.sessions.get<UserSession>()
                job.complete()
            }
        }

        // WHEN
        routing.call(uri = "/login")
        advanceTimeBy(99)

        // THEN
        assertEquals(UserSession(id = "123abc", count = 0), userSession)
    }

    @Test
    fun shouldUpdateCurrentSessionWhenLoggedIn() = runTest {
        val job = Job()
        var userSession: UserSession? = null

        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Sessions) {
                session<UserSession>()
            }

            handle(path = "/login") {
                call.sessions.set(UserSession(id = "123abc", count = 0))
                call.redirectToPath(path = "/update")
            }

            handle(path = "/update") {
                val session = call.sessions.get<UserSession>()
                call.sessions.set(session?.copy(count = session.count + 1))
                call.redirectToPath(path = "/user")
            }

            handle(path = "/user") {
                userSession = call.sessions.get<UserSession>()
                job.complete()
            }
        }

        // WHEN
        routing.call(uri = "/login")
        advanceTimeBy(99)

        // THEN
        assertEquals(UserSession(id = "123abc", count = 1), userSession)
    }

    @Test
    fun shouldClearSessionWhenLoggedOut() = runTest {
        val job = Job()
        var userSession: UserSession? = null

        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Sessions) {
                session<UserSession>()
            }

            handle(path = "/login") {
                call.sessions.set(UserSession(id = "123abc", count = 0))
                call.redirectToPath(path = "/user")
            }

            handle(path = "/user") {
                userSession = call.sessions.get<UserSession>()
                call.redirectToPath(path = "/logout")
            }

            handle(path = "/logout") {
                call.sessions.clear<UserSession>()
                call.redirectToPath(path = "/done")
            }

            handle(path = "/done") {
                userSession = call.sessions.get<UserSession>()
                job.complete()
            }
        }

        // WHEN
        routing.call(uri = "/login")
        advanceTimeBy(99)

        // THEN
        assertNull(userSession)
    }

    @Test
    fun shouldNestedRoutingGetParentSessionWhenHavingNestedRouting() = runTest {
        // GIVEN
        val job = Job()
        var userSession: UserSession? = null

        val parent = routing(parentCoroutineContext = coroutineContext + job) {
            install(Sessions) {
                session<UserSession>()
            }

            handle(path = "/login") {
                call.sessions.set(UserSession(id = "123abc", count = 0))
                call.redirectToPath(path = "/child/user")
            }
        }

        routing(
            rootPath = "/child",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            install(Sessions)

            handle(path = "/user") {
                userSession = call.sessions.get<UserSession>()
                job.complete()
            }
        }

        // WHEN
        parent.call(uri = "/login")
        advanceTimeBy(99)

        // THEN
        assertEquals(UserSession(id = "123abc", count = 0), userSession)
    }

    @Test
    fun shouldScopedSessionNotBeSharedWithParentWhenHavingNestedRouting() = runTest {
        // GIVEN
        val job = Job()
        var exception: Throwable? = null

        val parent = routing(parentCoroutineContext = coroutineContext + job) {
            install(Sessions)

            handle(path = "/logout") {
                runCatching {
                    call.sessions.get<UserSession>()
                }.onFailure { ex ->
                    exception = ex
                }
                job.complete()
            }
        }

        val routing = routing(
            rootPath = "/child",
            parent = parent,
            parentCoroutineContext = coroutineContext + job
        ) {
            install(Sessions) {
                session<UserSession>()
            }

            handle(path = "/login") {
                call.sessions.set(UserSession(id = "123abc", count = 0))
                call.redirectToPath(path = "/logout")
            }
        }

        // WHEN
        routing.call(uri = "/login")
        advanceTimeBy(99)

        // THEN
        assertIs<IllegalArgumentException>(exception)
        assertEquals(
            "Session data for type `class dev.programadorthi.routing.sessions.UserSession (Kotlin reflection is not available)` was not registered",
            exception?.message
        )
    }
}
