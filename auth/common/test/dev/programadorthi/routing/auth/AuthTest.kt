package dev.programadorthi.routing.auth

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.handle
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.sessions.Sessions
import dev.programadorthi.routing.sessions.session
import dev.programadorthi.routing.statuspages.StatusPages
import io.ktor.http.Parameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
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
            ApplicationCall(
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
            ApplicationCall(
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
            ApplicationCall(
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
        var principal: UserIdPrincipal? = UserIdPrincipal(name = "default value")

        // GIVEN
        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Sessions) {
                session<UserIdPrincipal>()
            }

            install(Authentication) {
                session<UserIdPrincipal>(name = UserIdPrincipal.NAME) {
                    validate { session -> session }

                    challenge {
                        ChallengeStatus.Approved(UserIdPrincipal(name = "Authenticated"))
                    }
                }
            }

            authenticate(UserIdPrincipal.NAME) {
                handle(path = "/user") {
                    principal = call.principal()
                }
            }
        }

        // WHEN
        routing.execute(
            ApplicationCall(
                application = routing.application,
                uri = "/user"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertEquals(UserIdPrincipal(name = "Authenticated"), principal)
    }

    @Test
    fun shouldAskForChallengeAndBeNotAuthorizedWhenChallengeHasDeniedStatus() = runTest {
        val job = Job()
        var exception: Throwable? = null
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

                    challenge {
                        challengeCalled = true
                        ChallengeStatus.Denied
                    }
                }
            }

            authenticate(UserIdPrincipal.NAME) {
                handle(path = "/user") {
                    principal = call.principal()
                }
            }
        }

        // WHEN
        routing.execute(
            ApplicationCall(
                application = routing.application,
                uri = "/user"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertTrue(challengeCalled)
        assertEquals(UserIdPrincipal(name = "default value"), principal)
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
            ApplicationCall(
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

        var principal: UserIdPrincipal? = UserIdPrincipal(name = "default value")

        // Simulating an external challenge flow
        launch(coroutineContext + job) {
            // Waiting ask for challenge
            challengeStart.receive()
            // Simulating a long login flow
            delay(500)

            challengeBlocking.send(ChallengeStatus.Approved(UserIdPrincipal(name = "Authenticated")))
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
                        challengeBlocking.receive()
                    }
                }
            }

            authenticate(UserIdPrincipal.NAME) {
                handle(path = "/user") {
                    principal = call.principal()
                }
            }
        }

        // WHEN
        routing.execute(
            ApplicationCall(
                application = routing.application,
                uri = "/user"
            )
        )
        advanceTimeBy(99)
        advanceTimeBy(500)

        // THEN
        assertEquals(UserIdPrincipal(name = "Authenticated"), principal)
    }

    @Test
    fun shouldParentManagerAuthenticationWhenHavingNestedRouting() = runTest {
        val job = Job()
        var exception: Throwable? = null

        // GIVEN
        val parent = routing(parentCoroutineContext = coroutineContext + job) {
            install(Sessions) {
                session<UserIdPrincipal>()
            }

            install(Authentication) {
                session<UserIdPrincipal>(name = UserIdPrincipal.NAME)
            }
        }

        val routing = routing(
            parent = parent,
            rootPath = "/child",
            parentCoroutineContext = coroutineContext + job
        ) {
            install(StatusPages) {
                exception<Throwable> { _, cause ->
                    exception = cause
                    job.complete()
                }
            }

            install(Sessions)
            install(Authentication)

            authenticate(UserIdPrincipal.NAME) {
                handle(path = "/user") {
                    error("Never reached code")
                }
            }
        }

        // WHEN
        routing.execute(
            ApplicationCall(
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
    fun shouldParentNeverUseChildAuthenticationWhenHavingNestedRouting() = runTest {
        val job = Job()

        // GIVEN
        val exception = assertFails {
            val parent = routing(parentCoroutineContext = coroutineContext + job) {
                install(Authentication) {
                    session<UserIdPrincipal>(name = UserIdPrincipal.NAME)
                }

                // WHEN
                // profile-config is a provider configured in child routing
                authenticate("profile-config") {
                    handle(path = "/profile") {
                        error("Never reached code")
                    }
                }
            }

            routing(
                parent = parent,
                rootPath = "/child",
                parentCoroutineContext = coroutineContext + job
            ) {
                install(Authentication) {
                    session<UserIdPrincipal>(name = "profile-config")
                }
            }
        }

        // THEN
        assertIs<IllegalArgumentException>(exception)
        assertEquals(
            "Authentication configuration with the name profile-config was not found. Make sure that you install Authentication plugin before you use it in Routing",
            exception.message
        )
    }

    @Test
    fun shouldChildHasHisOwnerAuthenticationWhenHavingNestedRouting() = runTest {
        val job = Job()
        var principal: UserIdPrincipal? = UserIdPrincipal(name = "default value")

        // GIVEN
        val parent = routing(parentCoroutineContext = coroutineContext + job) {
            // No-op for this test
        }

        val routing = routing(
            parent = parent,
            rootPath = "/child",
            parentCoroutineContext = coroutineContext + job
        ) {
            install(Sessions) {
                session<UserIdPrincipal>()
            }

            install(Authentication) {
                session<UserIdPrincipal>(name = UserIdPrincipal.NAME) {
                    validate { session -> session }
                    challenge {
                        ChallengeStatus.Approved(UserIdPrincipal(name = "Kotlin Routing"))
                    }
                }
            }

            authenticate(UserIdPrincipal.NAME) {
                handle(path = "/user") {
                    principal = call.principal()
                }
            }
        }

        // WHEN
        routing.execute(
            ApplicationCall(
                application = routing.application,
                uri = "/user"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertEquals(UserIdPrincipal(name = "Kotlin Routing"), principal)
    }

    @Test
    fun shouldParentAndChildHasHisOwnerAuthenticationWhenHavingNestedRouting() = runTest {
        data class ParentPrincipal(val name: String) : Principal
        data class ChildPrincipal(val name: String) : Principal

        val job = Job()
        var parentPrincipal: ParentPrincipal? = null
        var childPrincipal: ChildPrincipal? = null

        // GIVEN
        val parent = routing(parentCoroutineContext = coroutineContext + job) {
            install(Sessions) {
                session<ParentPrincipal>()
            }

            install(Authentication) {
                session<ParentPrincipal>(name = "parent-provider") {
                    validate { session -> session }
                    challenge {
                        ChallengeStatus.Approved(ParentPrincipal(name = "Parent Routing"))
                    }
                }
            }
        }

        val routing = routing(
            parent = parent,
            rootPath = "/nested",
            parentCoroutineContext = coroutineContext + job
        ) {
            install(Sessions) {
                session<ChildPrincipal>()
            }

            install(Authentication) {
                session<ChildPrincipal>(name = "child-provider") {
                    validate { session -> session }
                    challenge {
                        ChallengeStatus.Approved(ChildPrincipal(name = "Child Routing"))
                    }
                }
            }

            authenticate("parent-provider") {
                handle(path = "/parent") {
                    parentPrincipal = call.principal()
                }
            }

            authenticate("child-provider") {
                handle(path = "/child") {
                    childPrincipal = call.principal()
                }
            }
        }

        // WHEN
        routing.execute(
            ApplicationCall(
                application = routing.application,
                uri = "/parent"
            )
        )
        advanceTimeBy(99)
        routing.execute(
            ApplicationCall(
                application = routing.application,
                uri = "/child"
            )
        )
        advanceTimeBy(99)

        // THEN
        assertEquals(ParentPrincipal(name = "Parent Routing"), parentPrincipal)
        assertEquals(ChildPrincipal(name = "Child Routing"), childPrincipal)
    }
}
