/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.core.application

import dev.programadorthi.routing.core.Route
import io.ktor.util.AttributeKey

/**
 * Utility class to build a [RouteScopedPlugin] instance.
 **/
public abstract class RouteScopedPluginBuilder<PluginConfig : Any>(key: AttributeKey<PluginInstance>) :
    PluginBuilder<PluginConfig>(key) {
        /**
         * A [Route] to which this plugin was installed. Can be `null` if plugin in installed into [Application].
         **/
        public abstract val route: Route?
    }
