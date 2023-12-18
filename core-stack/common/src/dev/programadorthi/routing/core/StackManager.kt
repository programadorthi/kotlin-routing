package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.ApplicationStopped
import dev.programadorthi.routing.core.application.createApplicationPlugin
import io.ktor.util.AttributeKey

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

public val ApplicationCall.previous: StackApplicationCall?
    get() = stackManager.lastOrNull()

/**
 * A plugin that provides a stack manager on each call
 */
public val StackRouting: ApplicationPlugin<Unit> = createApplicationPlugin("StackRouting") {
    val stackManager = StackManager(application)

    onCall { call ->
        call.stackManager = stackManager
    }
}

internal class StackManager(val application: Application) {
    private val stack = mutableListOf<StackApplicationCall>()

    init {
        val environment = application.environment
        val providerId = "${environment.parentRouting}#${environment.rootPath}"
        register(providerId = providerId, stackManager = this)
        application.environment.monitor.subscribe(ApplicationStopped) {
            unregister(providerId)
        }
    }

    fun lastOrNull(): StackApplicationCall? = stack.lastOrNull()

    fun update(call: ApplicationCall) {
        // Check if route should be out of the stack
        if (call.stackNeglect) {
            return
        }

        when (call.routeMethod) {
            StackRouteMethod.Push -> {
                stack += when {
                    call.name.isBlank() -> StackApplicationCall.Push(
                        application = application,
                        uri = call.uri,
                        parameters = call.parameters,
                    )

                    else -> StackApplicationCall.PushNamed(
                        application = application,
                        name = call.name,
                        parameters = call.parameters,
                    )
                }
            }

            StackRouteMethod.Replace -> {
                stack.removeLastOrNull()
                stack += call.toReplace()
            }

            StackRouteMethod.ReplaceAll -> {
                stack.clear()
                stack += call.toReplace()
            }

            StackRouteMethod.Pop -> {
                // Pop in a valid state only
                if (call.uri == stack.lastOrNull()?.uri) {
                    stack.removeLast()
                }
            }

            else -> Unit
        }
    }

    fun toSave(): List<StackApplicationCall> = buildList {
        addAll(stack)
    }

    fun toRestore(previous: List<StackApplicationCall>) {
        stack.clear()
        stack.addAll(previous)
    }

    private fun ApplicationCall.toReplace(): StackApplicationCall = when {
        name.isBlank() -> StackApplicationCall.Replace(
            all = routeMethod == StackRouteMethod.ReplaceAll,
            application = application,
            uri = uri,
            parameters = parameters,
        )

        else -> StackApplicationCall.ReplaceNamed(
            all = routeMethod == StackRouteMethod.ReplaceAll,
            application = application,
            name = name,
            parameters = parameters,
        )
    }

    internal companion object {
        private val subscriptions = mutableMapOf<String, StackManager>()
        internal var stackManagerNotifier: StackManagerNotifier? = null

        private fun register(providerId: String, stackManager: StackManager) {
            subscriptions += providerId to stackManager
            stackManagerNotifier?.onRegistered(providerId, stackManager)
        }

        private fun unregister(providerId: String) {
            subscriptions -= providerId
            stackManagerNotifier?.onUnRegistered(providerId)
        }

        fun subscriptions(): Map<String, StackManager> = buildMap {
            putAll(subscriptions)
        }
    }
}
