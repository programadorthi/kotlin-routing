/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.auth

import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey
import kotlin.reflect.KClass

/**
 * An authentication context for a call.
 * @param call instance of [ApplicationCall] this context is for.
 */
public class AuthenticationContext(call: ApplicationCall) {
    public var call: ApplicationCall = call
        private set

    private val errors = HashMap<Any, AuthenticationFailedCause>()

    internal val combinedPrincipal: CombinedPrincipal = CombinedPrincipal()

    /**
     * All registered errors during auth procedure (only [AuthenticationFailedCause.Error]).
     */
    public val allErrors: List<AuthenticationFailedCause.Error>
        get() = errors.values.filterIsInstance<AuthenticationFailedCause.Error>()

    /**
     * All authentication failures during auth procedure including missing or invalid credentials.
     */
    public val allFailures: List<AuthenticationFailedCause>
        get() = errors.values.toList()

    /**
     * Appends an error to the errors list. Overwrites if already registered for the same [key].
     */
    public fun error(
        key: Any,
        cause: AuthenticationFailedCause,
    ) {
        errors[key] = cause
    }

    /**
     * Gets an [AuthenticationProcedureChallenge] for this context.
     */
    public val challenge: AuthenticationProcedureChallenge = AuthenticationProcedureChallenge()

    /**
     * Sets an authenticated principal for this context.
     */
    public fun principal(principal: Principal) {
        combinedPrincipal.add(null, principal)
    }

    /**
     * Sets an authenticated principal for this context from provider with name [provider].
     */
    public fun principal(
        provider: String? = null,
        principal: Principal,
    ) {
        combinedPrincipal.add(provider, principal)
    }

    /**
     * Retrieves a principal of the type [T] from provider with name [provider], if any.
     */
    public inline fun <reified T : Principal> principal(provider: String? = null): T? {
        return principal(provider, T::class)
    }

    /**
     * Retrieves a principal of the type [T], if any.
     */
    public fun <T : Principal> principal(
        provider: String?,
        klass: KClass<T>,
    ): T? {
        return combinedPrincipal.get(provider, klass)
    }

    /**
     * Requests a challenge to be sent to the client if none of mechanisms can authenticate a user.
     */
    public fun challenge(
        key: Any,
        cause: AuthenticationFailedCause,
        function: ChallengeFunction,
    ) {
        error(key, cause)
        challenge.register.add(cause to function)
    }

    public companion object {
        private val AttributeKey = AttributeKey<AuthenticationContext>("AuthContext")

        internal fun from(call: ApplicationCall): AuthenticationContext {
            val existingContext = call.attributes.getOrNull(AttributeKey)
            if (existingContext != null) {
                existingContext.call = call
                return existingContext
            }
            val context = AuthenticationContext(call)
            call.attributes.put(AttributeKey, context)
            return context
        }
    }
}
