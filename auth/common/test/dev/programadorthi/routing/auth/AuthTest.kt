package dev.programadorthi.routing.auth

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.handle
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.sessions.Sessions
import dev.programadorthi.routing.sessions.session
import dev.programadorthi.routing.statuspages.StatusPages
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthTest {

    data class UserIdPrincipal(val name: String) : Principal {
        companion object {
            const val NAME = "user-id-principal"
        }
    }

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
    fun shouldNeverHandleARouteWhenAuthorizationIsRequired() = runTest {
        val job = Job()
        var exception: Throwable? = null

        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StatusPages) {
                exception<Throwable> { _, cause ->
                    exception = cause
                    job.complete()
                }
            }

            install(Sessions) {
                session<UserIdPrincipal>()
            }

            install(Authentication) {
                session<UserIdPrincipal>(name = UserIdPrincipal.NAME)
            }

            authenticate(UserIdPrincipal.NAME) {
                handle(path = "/user") {
                    error("Never reached code")
                }
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/user"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertIs<RoutingUnauthorizedException>(exception)
        assertEquals("Unauthorized route access to /user", exception?.message)
    }

    @Test
    fun shouldHandleARouteWithNullPrincipalWhenAuthorizationIsRequired() = runTest {
        val job = Job()
        var principal: UserIdPrincipal? = UserIdPrincipal(name = "default value")

        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Sessions) {
                session<UserIdPrincipal>()
            }

            install(Authentication) {
                session<UserIdPrincipal>(name = UserIdPrincipal.NAME)
            }

            authenticate(UserIdPrincipal.NAME, optional = true) {
                handle(path = "/user") {
                    principal = call.principal<UserIdPrincipal>()
                }
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/user"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNull(principal)
    }

    @Test
    fun shouldNeverAuthenticateWhenAuthenticationValidateReturnsNull() = runTest {
        val job = Job()
        var exception: Throwable? = null

        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StatusPages) {
                exception<Throwable> { _, cause ->
                    exception = cause
                    job.complete()
                }
            }

            install(Sessions) {
                session<UserIdPrincipal>()
            }

            install(Authentication) {
                session<UserIdPrincipal>(name = UserIdPrincipal.NAME) {
                    validate { null }
                }
            }

            authenticate(UserIdPrincipal.NAME) {
                handle(path = "/user") {
                    error("Never reached code")
                }
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/user"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertIs<RoutingUnauthorizedException>(exception)
        assertEquals("Unauthorized route access to /user", exception?.message)
    }

    @Test
    fun shouldAskForChallengeAndBeAuthorizedWhenChallengeHasApprovedStatus() = runTest {
        val job = Job()
        var message = ""
        var challengeCalled = false
        var principal: UserIdPrincipal? = UserIdPrincipal(name = "default value")

        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Sessions) {
                session<UserIdPrincipal>()
            }

            install(Authentication) {
                session<UserIdPrincipal>(name = UserIdPrincipal.NAME) {
                    validate { session -> session }

                    challenge { session ->
                        principal = session
                        challengeCalled = true
                        ChallengeStatus.Approved
                    }
                }
            }

            authenticate(UserIdPrincipal.NAME) {
                handle(path = "/user") {
                    message = "Hey, I was called because challenge had Approved status"
                }
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/user"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertTrue(challengeCalled)
        assertNull(principal)
        assertEquals("Hey, I was called because challenge had Approved status", message)
    }

    @Test
    fun shouldAskForChallengeAndBeNotAuthorizedWhenChallengeHasDeniedStatus() = runTest {
        val job = Job()
        var exception: Throwable? = null
        var message = ""
        var challengeCalled = false
        var principal: UserIdPrincipal? = UserIdPrincipal(name = "default value")

        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StatusPages) {
                exception<Throwable> { _, cause ->
                    exception = cause
                    job.complete()
                }
            }

            install(Sessions) {
                session<UserIdPrincipal>()
            }

            install(Authentication) {
                session<UserIdPrincipal>(name = UserIdPrincipal.NAME) {
                    validate { session -> session }

                    challenge { session ->
                        principal = session
                        challengeCalled = true
                        ChallengeStatus.Denied
                    }
                }
            }

            authenticate(UserIdPrincipal.NAME) {
                handle(path = "/user") {
                    message = "Hey, I was not called because challenge had Denied status"
                }
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/user"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertTrue(challengeCalled)
        assertNull(principal)
        assertEquals("", message)
        assertIs<RoutingUnauthorizedException>(exception)
        assertEquals("Unauthorized route access to /user", exception?.message)
    }

    @Test
    fun shouldAskForChallengeAndRedirectToAuthenticationWhenChallengeDoesARedirection() = runTest {
        val job = Job()
        var exception: Throwable? = null
        var result: ApplicationCall? = null

        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StatusPages) {
                exception<RoutingRedirectToAuthenticateException> { _, cause ->
                    // Challenge redirection always throw a RoutingRedirectToAuthenticateException
                    exception = cause
                    job.complete()
                }
            }

            install(Sessions) {
                session<UserIdPrincipal>()
            }

            install(Authentication) {
                session<UserIdPrincipal>(name = UserIdPrincipal.NAME) {
                    validate { session -> session }

                    challenge(redirectUrl = "/login")
                }
            }

            authenticate(UserIdPrincipal.NAME) {
                handle(path = "/user") {
                    error("Never reached code")
                }
            }

            handle(path = "/login") {
                result = call
                job.complete()
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/user"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/login", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(RouteMethod.Empty, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
        assertIs<RoutingRedirectToAuthenticateException>(exception)
        assertEquals("Redirecting /user to authentication route /login", exception?.message)
    }

    @Test
    fun shouldAnswerChallengeBlockingWhenWaitingToSolveIt() = runTest {
        val job = Job()
        val challengeStart = Channel<Unit>()
        val challengeBlocking = Channel<ChallengeStatus>()

        var message = ""
        var challengeAsked = false
        var challengeCalled = false

        // Simulating an external challenge flow
        launch(coroutineContext + job) {
            challengeStart.receive() // Waiting ask for challenge
            challengeAsked = true
            delay(500) // Simulating a long flow
            challengeBlocking.send(ChallengeStatus.Approved)
        }

        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Sessions) {
                session<UserIdPrincipal>()
            }

            install(Authentication) {
                session<UserIdPrincipal>(name = UserIdPrincipal.NAME) {
                    validate { session -> session }

                    challenge {
                        challengeStart.send(Unit) // Simulating starting external challenge flow
                        challengeCalled = true
                        challengeBlocking.receive()
                    }
                }
            }

            authenticate(UserIdPrincipal.NAME) {
                handle(path = "/user") {
                    message = "Hey, I was called because challenge had Approved status"
                }
            }
        }

        // WHEN
        routing.execute(
            BasicApplicationCall(
                application = routing.application,
                uri = "/user"
            )
        )
        advanceTimeBy(99)
        advanceTimeBy(500)

        // THEN
        assertTrue(challengeAsked)
        assertTrue(challengeCalled)
        assertEquals("Hey, I was called because challenge had Approved status", message)
    }

    // TODO: Add support and tests to Nested routes
}
