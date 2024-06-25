package dev.programadorthi.routing.voyager.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.screen.Screen
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val HASH_PREFIX = "/#"

internal actual fun ApplicationCall.shouldNeglect(): Boolean = neglect

internal actual suspend fun ApplicationCall.platformPush(
    routing: Routing,
    body: suspend () -> Screen,
    fallback: suspend () -> Unit,
) {
    if (routing.historyMode == VoyagerHistoryMode.Memory) {
        fallback()
        return
    }
    window.history.pushState(
        title = "routing",
        url = uriToAddressBar(),
        data = serialize(),
    )
}

internal actual suspend fun ApplicationCall.platformReplace(
    routing: Routing,
    body: suspend () -> Screen,
    fallback: suspend () -> Unit,
) {
    if (routing.historyMode == VoyagerHistoryMode.Memory) {
        fallback()
        return
    }
    window.history.replaceState(
        title = "routing",
        url = uriToAddressBar(),
        data = serialize(),
    )
}

internal actual suspend fun ApplicationCall.platformReplaceAll(
    routing: Routing,
    body: suspend () -> Screen,
    fallback: suspend () -> Unit,
) {
    if (routing.historyMode == VoyagerHistoryMode.Memory) {
        fallback()
        return
    }
    while (true) {
        window.history.replaceState(
            title = "",
            url = null,
            data = null,
        )
        val forceBreak =
            runCatching {
                withTimeout(2_000) {
                    suspendCoroutine { continuation ->
                        window.onpopstate = { event ->
                            val state = event.state.deserialize()
                            continuation.resume(state == null)
                        }
                        window.history.go(-1)
                    }
                }
            }.getOrDefault(true)
        if (forceBreak) {
            break
        }
    }

    window.history.replaceState(
        title = "routing",
        url = uriToAddressBar(),
        data = serialize(),
    )

    listenToOnPopState(routing)
}

@Composable
internal actual fun Routing.restoreState(onState: (Any) -> Unit) {
    val routing = this

    LaunchedEffect(Unit) {
        window.onpageshow = {
            // First time or page refresh we try continue from last state
            val state = window.history.state
            if (state != null) {
                stateToCall(state)?.let(onState)
            } else {
                val hashSanitized = window.location.hash.removePrefix("#")
                val destination =
                    when {
                        historyMode == VoyagerHistoryMode.Hash && hashSanitized.isNotBlank() -> hashSanitized
                        else -> window.location.run { pathname + search + hash }
                    }
                onState(destination)
            }
        }

        listenToOnPopState(routing)
    }
}

internal fun Routing.popWindowHistory() {
    val routing = this
    var job: Job? = null
    job =
        application.launch {
            window.history.replaceState(
                title = "",
                url = null,
                data = null,
            )
            try {
                // Why timeout?
                // Because go(-1) when there is no history don't trigger window.onpopstate
                withTimeout(2_000) {
                    suspendCoroutine { continuation ->
                        listenToOnPopState(routing) {
                            continuation.resume(Unit)
                        }
                        window.history.go(-1)
                    }
                }
            } finally {
                listenToOnPopState(routing)
                job?.cancel()
            }
        }
}

private fun ApplicationCall.uriToAddressBar(): String {
    return when {
        application.historyMode != VoyagerHistoryMode.Hash -> uri

        else -> HASH_PREFIX + uri
    }
}

private fun listenToOnPopState(
    routing: Routing,
    onPop: () -> Unit = {},
) {
    window.onpopstate = { event ->
        routing.tryNotifyTheRoute(state = event.state)
        onPop()
    }
}

private fun Routing.tryNotifyTheRoute(state: Any?) {
    val call = stateToCall(state) ?: return
    execute(call)
}

private fun Routing.stateToCall(state: Any?): ApplicationCall? {
    val composeHistoryState = state.deserialize() ?: return null
    val call = composeHistoryState.toCall(application)
    call.neglect = true
    return call
}
