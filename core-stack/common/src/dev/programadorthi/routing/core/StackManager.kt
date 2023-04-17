package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.createApplicationPlugin
import io.ktor.util.AttributeKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val StackManagerAttributeKey: AttributeKey<StackManager> =
    AttributeKey("StackManagerAttributeKey")

internal var ApplicationCall.stackManager: StackManager
    get() = application.stackManager
    private set(value) {
        application.stackManager = value
    }

internal var Application.stackManager: StackManager
    get() = attributes[StackManagerAttributeKey]
    private set(value) {
        attributes.put(StackManagerAttributeKey, value)
    }

/**
 * A plugin that provides a stack manager on each call
 */
public val StackRouting: ApplicationPlugin<Unit> = createApplicationPlugin("StackRouting") {
    val stackManager = StackManager()

    onCall { call ->
        call.stackManager = stackManager
    }
}

internal class StackManager {
    private val mutex = Mutex()
    private val pathStack = mutableListOf<String>()

    suspend fun last(): String {
        mutex.withLock {
            return pathStack.lastOrNull() ?: ""
        }
    }

    suspend fun update(call: ApplicationCall) {
        mutex.withLock {
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

                else -> Unit
            }
        }
    }
}
