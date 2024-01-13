/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.auth

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.redirectToPath
import dev.programadorthi.routing.sessions.get
import dev.programadorthi.routing.sessions.sessions
import io.ktor.http.Url
import kotlin.reflect.KClass

/**
 * A session-based [Authentication] provider.
 * @see [session]
 *
 * @property type of session
 * @property challengeFunction to be used if there is no session
 * @property validator applied to an application all and session providing a [Principal]
 */
public class SessionAuthenticationProvider<T : Any> private constructor(
    config: Config<T>,
) : AuthenticationProvider(config) {
    public val type: KClass<T> = config.type

    private val challengeFunction: SessionAuthChallengeFunction<T> = config.challengeFunction

    private val validator: AuthenticationFunction<T> = config.validator

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call
        val session = call.sessions.get(type)
        val principal = session?.let { validator(call, it) }

        if (principal != null) {
            context.principal(name, principal)
        } else {
            val cause =
                if (session == null) {
                    AuthenticationFailedCause.NoCredentials
                } else {
                    AuthenticationFailedCause.InvalidCredentials
                }

            @Suppress("NAME_SHADOWING")
            context.challenge(SessionAuthChallengeKey, cause) { _, call ->
                challengeFunction(SessionChallengeContext(call), principal)
            }
        }
    }

    /**
     * A configuration for the [session] authentication provider.
     */
    public class Config<T : Any>
        @PublishedApi
        internal constructor(
            name: String?,
            internal val type: KClass<T>,
        ) : AuthenticationProvider.Config(name) {
            internal var validator: AuthenticationFunction<T> = UninitializedValidator

            internal var challengeFunction: SessionAuthChallengeFunction<T> = {
                call.throwNotAuthorized()
            }

            /**
             * Specifies a response to send back if authentication failed.
             */
            public fun challenge(block: SessionAuthChallengeFunction<T>) {
                challengeFunction = block
            }

            /**
             * Specifies a response to send back if authentication failed.
             */
            public fun challenge(redirectUrl: String) {
                challenge {
                    call.redirectToPath(path = redirectUrl)
                    ChallengeStatus.Redirected(destination = redirectUrl)
                }
            }

            /**
             * Specifies a response to send back if authentication failed.
             */
            public fun challenge(redirect: Url) {
                challenge(redirect.toString())
            }

            /**
             * Sets a validation function that checks a given [T] session instance and returns [Principal],
             * or null if the session does not correspond to an authenticated principal.
             */
            public fun validate(block: suspend ApplicationCall.(T) -> Principal?) {
                check(validator === UninitializedValidator) { "Only one validator could be registered" }
                validator = block
            }

            private fun verifyConfiguration() {
                check(validator !== UninitializedValidator) {
                    "It should be a validator supplied to a session auth provider"
                }
            }

            @PublishedApi
            internal fun buildProvider(): SessionAuthenticationProvider<T> {
                verifyConfiguration()
                return SessionAuthenticationProvider(this)
            }
        }

    public companion object {
        private val UninitializedValidator: suspend ApplicationCall.(Any) -> Principal? = {
            error("It should be a validator supplied to a session auth provider")
        }
    }
}

/**
 * Installs the session [Authentication] provider.
 * This provider provides the ability to authenticate a user that already has an associated session.
 *
 * To learn how to configure the session provider, see [Session authentication](https://ktor.io/docs/session-auth.html).
 */
public inline fun <reified T : Principal> AuthenticationConfig.session(name: String? = null) {
    session<T>(name) {
        validate { session -> session }
    }
}

/**
 * Installs the session [Authentication] provider.
 * This provider provides the ability to authenticate a user that already has an associated session.
 *
 * To learn how to configure the session provider, see [Session authentication](https://ktor.io/docs/session-auth.html).
 */
public inline fun <reified T : Any> AuthenticationConfig.session(
    name: String? = null,
    configure: SessionAuthenticationProvider.Config<T>.() -> Unit,
) {
    val provider = SessionAuthenticationProvider.Config(name, T::class).apply(configure).buildProvider()
    register(provider)
}

/**
 * A context for [SessionAuthChallengeFunction].
 */
public class SessionChallengeContext(
    public val call: ApplicationCall,
)

/**
 * Specifies what to send back if session authentication fails.
 */
public typealias SessionAuthChallengeFunction<T> = suspend SessionChallengeContext.(T?) -> ChallengeStatus

/**
 * A key used to register authentication challenge.
 */
@Suppress("PropertyName")
public const val SessionAuthChallengeKey: String = "SessionAuth"

@Throws(RoutingUnauthorizedException::class)
internal fun ApplicationCall.throwNotAuthorized(): Nothing {
    throw RoutingUnauthorizedException("Unauthorized route access to $uri")
}
