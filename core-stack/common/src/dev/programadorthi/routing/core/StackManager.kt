package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.ApplicationStarted
import dev.programadorthi.routing.core.application.ApplicationStopped
import dev.programadorthi.routing.core.application.createApplicationPlugin
import dev.programadorthi.routing.core.application.hooks.CallSetup
import dev.programadorthi.routing.core.application.hooks.ResponseSent
import dev.programadorthi.routing.core.application.pluginOrNull
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import io.ktor.util.putAll
import io.ktor.utils.io.KtorDsl
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val AfterRestorationAttributeKey: AttributeKey<Boolean> =
    AttributeKey("AfterRestorationAttributeKey")

private val PreviousCallAttributeKey: AttributeKey<ApplicationCall> =
    AttributeKey("PreviousCallAttributeKey")

private val StackManagerAttributeKey: AttributeKey<StackManager> =
    AttributeKey("StackManagerAttributeKey")

private var ApplicationCall.isRestored: Boolean
    get() = attributes.getOrNull(AfterRestorationAttributeKey) ?: false
    private set(value) {
        attributes.put(AfterRestorationAttributeKey, value)
    }

internal var Application.stackManager: StackManager
    get() = attributes[StackManagerAttributeKey]
    private set(value) {
        attributes.put(StackManagerAttributeKey, value)
    }

internal fun Application.checkPluginInstalled() {
    checkNotNull(pluginOrNull(StackRouting)) {
        "StackRouting plugin not installed"
    }
}

public var ApplicationCall.previousCall: ApplicationCall?
    get() = attributes.getOrNull(PreviousCallAttributeKey)
    internal set(value) {
        if (value != null) {
            attributes.put(PreviousCallAttributeKey, value)
        } else {
            attributes.remove(PreviousCallAttributeKey)
        }
    }

/**
 * A plugin that provides a stack manager on each call
 */
public val StackRouting: ApplicationPlugin<StackRoutingConfig> =
    createApplicationPlugin(
        name = "StackRouting",
        createConfiguration = ::StackRoutingConfig,
    ) {
        val stackManager = StackManager(application, pluginConfig)

        application.stackManager = stackManager

        on(CallSetup) { call ->
            if (call.routeMethod.isStackMethod()) {
                call.application.checkPluginInstalled()
            }
            if (call.previousCall == null) {
                call.previousCall = stackManager.lastOrNull()
            }
        }

        on(ResponseSent) { call ->
            stackManager.update(call)
        }
    }

@KtorDsl
public class StackRoutingConfig {
    /**
     * Used for Android or other that have process death restoration
     */
    public var emitAfterRestoration: Boolean = true
}

internal class StackManager(
    private val application: Application,
    private val config: StackRoutingConfig,
) {
    private val mutex = Mutex()
    private val stack = mutableListOf<ApplicationCall>()

    init {
        val environment = application.environment
        val providerId = "${environment.parentRouting}#${environment.rootPath}"
        environment.monitor.apply {

            subscribe(ApplicationStarted) {
                register(providerId = providerId, stackManager = this@StackManager)
            }

            subscribe(ApplicationStopped) {
                unregister(providerId)
            }
        }
    }

    suspend fun lastOrNull(): ApplicationCall? =
        mutex.withLock {
            stack.lastOrNull()
        }

    suspend fun toPop(): ApplicationCall? =
        mutex.withLock {
            stack.removeLastOrNull()
        }

    suspend fun update(call: ApplicationCall) =
        mutex.withLock {
            // Check if route should be out of the stack
            if (call.neglect) {
                return@withLock
            }

            // State restoration emit last call again
            if (call.isRestored) {
                stack += call
                return@withLock
            }

            when (call.routeMethod) {
                StackRouteMethod.Push -> {
                    stack += call
                }

                StackRouteMethod.Replace -> {
                    stack.removeLastOrNull()?.let { toNotify ->
                        val notify = toNotify.copy(routeMethod = StackRouteMethod.Pop)
                        notify.previousCall = call
                        // Notify previous route that it will be popped
                        executeCallWithNeglect(call = notify)
                    }
                    stack += call
                }

                StackRouteMethod.ReplaceAll -> {
                    var previousCall = call
                    while (true) {
                        val toNotify = stack.removeLastOrNull() ?: break
                        val notify = toNotify.copy(routeMethod = StackRouteMethod.Pop)
                        notify.previousCall = previousCall
                        // Notify previous route that it will be popped
                        executeCallWithNeglect(call = notify)
                        previousCall = toNotify
                    }
                    stack += call
                }

                StackRouteMethod.Pop -> {
                    // Don't be confused. The real route was popped on pop() function
                    // Here we are notifying previous route with popped parameters ;P
                    stack.lastOrNull()?.let { toNotify ->
                        val notify = toNotify.copy(parameters = call.parameters)
                        notify.previousCall = call
                        executeCallWithNeglect(call = notify)
                    }
                }

                else -> Unit
            }
        }

    fun toSave(): String = stack.map { it.toState() }.toJson()

    fun toRestore(saved: String?) {
        if (saved.isNullOrBlank()) return

        val states =
            saved.toStateList().map { stackState ->
                ApplicationCall(
                    application = application,
                    name = stackState.name,
                    uri = stackState.uri,
                    routeMethod = RouteMethod(stackState.routeMethod),
                    parameters = parametersOf(stackState.parameters),
                )
            }

        stack.clear()
        stack.addAll(states)
        // On platform that have state restoration we need to emit again the last item to notify
        if (config.emitAfterRestoration) {
            val call = stack.removeLastOrNull() ?: return
            call.isRestored = true
            call.previousCall = stack.lastOrNull()
            // Don't neglect to put again on the stack
            executeCallWithNeglect(call = call, neglect = false)
        }
    }

    private fun executeCallWithNeglect(
        call: ApplicationCall?,
        neglect: Boolean = true,
    ) {
        // We need neglect to avoid put again on the stack
        val callToNeglect = call?.toNeglect(neglect = neglect) ?: return
        application.pluginOrNull(Routing)?.execute(callToNeglect)
    }

    private fun ApplicationCall.copy(
        parameters: Parameters = this.parameters,
        routeMethod: RouteMethod = this.routeMethod,
    ): ApplicationCall =
        ApplicationCall(
            application = this.application,
            name = this.name,
            uri = this.uri,
            parameters = parameters,
            routeMethod = routeMethod,
            attributes =
                Attributes().apply {
                    putAll(this@copy.attributes)
                },
        )

    internal companion object {
        private val subscriptions = mutableMapOf<String, StackManager>()
        internal var stackManagerNotifier: StackManagerNotifier? = null

        private fun register(
            providerId: String,
            stackManager: StackManager,
        ) {
            subscriptions += providerId to stackManager
            stackManagerNotifier?.onRegistered(providerId, stackManager)
        }

        private fun unregister(providerId: String) {
            subscriptions -= providerId
            stackManagerNotifier?.onUnRegistered(providerId)
        }

        fun subscriptions(): Map<String, StackManager> =
            buildMap {
                putAll(subscriptions)
            }
    }
}
