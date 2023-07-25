/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.sessions

import dev.programadorthi.routing.core.application.RouteScopedPlugin
import dev.programadorthi.routing.core.application.createRouteScopedPlugin
import dev.programadorthi.routing.core.application.hooks.ResponseSent
import dev.programadorthi.routing.core.application.log
import io.ktor.util.AttributeKey

internal val SessionProvidersKey = AttributeKey<List<SessionProvider<*>>>("SessionProvidersKey")

/**
 * A plugin that provides a mechanism to persist data between different HTTP requests.
 * Typical use cases include storing a logged-in user's ID, the contents of a shopping basket,
 * or keeping user preferences on the client.
 * In Ktor, you can implement sessions by using cookies or custom headers,
 * choose whether to store session data on the server or pass it to the client,
 * sign and encrypt session data and more.
 *
 * You can learn more from [Sessions](https://ktor.io/docs/sessions.html).
 * @property providers list of session providers
 */
public val Sessions: RouteScopedPlugin<SessionsConfig> = createRouteScopedPlugin("Sessions", ::SessionsConfig) {
    val providers = pluginConfig.providers.toList()
    val logger = application.log

    application.attributes.put(SessionProvidersKey, providers)

    onCall { call ->
        // For each call, call each provider and retrieve session data if needed.
        // Capture data in the attribute's value
        val providerData = providers.associateBy({ it.name }) {
            it.receiveSessionData(call)
        }

        if (providerData.isEmpty()) {
            logger.trace("No sessions found for ${call.uri}")
        } else {
            val sessions = providerData.keys.joinToString()
            logger.trace("Sessions found for ${call.uri}: $sessions")
        }
        val sessionData = SessionData(providerData)
        call.attributes.put(SessionDataKey, sessionData)
    }

    // When response is being sent, call each provider to update/remove session data
    on(ResponseSent) { call ->
        val sessionData = call.attributes.getOrNull(SessionDataKey) ?: return@on

        sessionData.providerData.values.forEach { data ->
            logger.trace("Sending session data for ${call.uri}: ${data.provider.name}")
            data.sendSessionData(call)
        }

        sessionData.commit()
    }
}
