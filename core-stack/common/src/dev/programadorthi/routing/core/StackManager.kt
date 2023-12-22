package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.ApplicationPlugin
import dev.programadorthi.routing.core.application.ApplicationStarted
import dev.programadorthi.routing.core.application.ApplicationStopped
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.application.createApplicationPlugin
import dev.programadorthi.routing.core.application.hooks.CallSetup
import dev.programadorthi.routing.core.application.hooks.ResponseSent
import dev.programadorthi.routing.core.application.pluginOrNull
import io.ktor.http.parametersOf
import io.ktor.util.AttributeKey
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext

private val StackManagerAttributeKey: AttributeKey<StackManager> =
    AttributeKey("StackManagerAttributeKey")

internal var Application.stackManager: StackManager
    get() = attributes[StackManagerAttributeKey]
    private set(value) {
        attributes.put(StackManagerAttributeKey, value)
    }

internal var ApplicationCall.stackManager: StackManager
    get() = application.stackManager
    private set(value) {
        application.stackManager = value
    }

internal fun Application.checkPluginInstalled() {
    checkNotNull(pluginOrNull(StackRouting)) {
        "StackRouting plugin not installed"
    }
}

public fun PipelineContext<*, ApplicationCall>.previousCall(): ApplicationCall? {
    return call.stackManager.lastOrNull()
}

/**
 * A plugin that provides a stack manager on each call
 */
public val StackRouting: ApplicationPlugin<StackRoutingConfig> = createApplicationPlugin(
    name = "StackRouting",
    createConfiguration = ::StackRoutingConfig,
) {
    val stackManager = StackManager(application, pluginConfig)

    on(CallSetup) { call ->
        if (call.routeMethod.isStackMethod()) {
            call.application.checkPluginInstalled()
        }
        call.stackManager = stackManager
    }

    on(ResponseSent) { call ->
        call.stackManager.update(call)
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

    fun lastOrNull(): ApplicationCall? = stack.lastOrNull()

    fun toPop(): ApplicationCall? = stack.removeLastOrNull()

    fun update(call: ApplicationCall) {
        // Check if route should be out of the stack
        if (call.neglect || call.routeMethod.isStackPop()) {
            return
        }

        when (call.routeMethod) {
            StackRouteMethod.Push -> {
                stack += call
            }

            StackRouteMethod.Replace -> {
                stack.removeLastOrNull()
                stack += call
            }

            StackRouteMethod.ReplaceAll -> {
                stack.clear()
                stack += call
            }

            else -> Unit
        }
    }

    fun toSave(): String = stack.map { it.toState() }.toJson()

    fun toRestore(saved: String?) {
        if (saved.isNullOrBlank()) return

        val states = saved.toStateList().map { stackState ->
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
        tryEmitLastItem()
    }

    // On Android after restoration we need to emit again the last item to notify
    // We need neglect to avoid put again on the stack
    private fun tryEmitLastItem() {
        val item = stack.removeLastOrNull()

        if (!config.emitAfterRestoration || item == null) return

        application.pluginOrNull(Routing)?.execute(item)
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
