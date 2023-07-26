package dev.programadorthi.routing.sessions

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.redirectToPath
import dev.programadorthi.routing.core.handle
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import io.ktor.http.Parameters
import io.ktor.util.Attributes
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

    class BasicApplicationCall(
        override val application: Application,
        override val name: String = "",
        override val uri: String = "",
        override val parameters: Parameters = Parameters.Empty,
    ) : ApplicationCall {
        override val attributes: Attributes = Attributes()

        override val routeMethod: RouteMethod get() = RouteMethod.Empty
    }

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
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/login"
            )
        )
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
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/login"
            )
        )
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
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/login"
            )
        )
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
        parent.execute(
            BasicApplicationCall(
                application = parent.application,
                uri = "/login"
            )
        )
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
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/login"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertIs<IllegalArgumentException>(exception)
        assertEquals(
            "Session data for type `class dev.programadorthi.routing.sessions.UserSession (Kotlin reflection is not available)` was not registered",
            exception?.message
        )
    }
}
