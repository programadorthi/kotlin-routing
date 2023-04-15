/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.createApplicationPlugin

public val StackRouting: ApplicationPlugin<Unit> = createApplicationPlugin("StackRouting") {
    // TODO: How to deal with concurrency?
    val pathStack = mutableListOf<String>()

    on(StackBeforeCall) { call ->
        if (call is StackApplicationCall.Pop) {
            call.uriToPop = pathStack.lastOrNull() ?: ""
        }
    }

    on(StackAfterCall) { call ->
        when (call.routeMethod) {
            StackRouteMethod.Push -> {
                pathStack += call.uri
            }

            StackRouteMethod.Replace -> {
                pathStack.removeLastOrNull()
                pathStack += call.uri
            }

            StackRouteMethod.ReplaceAll -> {
                pathStack.clear()
                pathStack += call.uri
            }
            // We are popping here instead of before call to pop when success only
            StackRouteMethod.Pop -> {
                pathStack.removeLastOrNull()
            }
        }
    }
}
