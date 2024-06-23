package dev.programadorthi.routing.compose.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.programadorthi.routing.compose.popResult
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.replace
import kotlinx.browser.window
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val HASH_PREFIX = "/#"

internal actual fun ApplicationCall.shouldNeglect(): Boolean = neglect

internal actual suspend fun ApplicationCall.platformPush(
    routing: Routing,
    fallback: () -> Unit,
) {
    if (routing.historyMode == ComposeHistoryMode.Memory) {
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
    fallback: () -> Unit,
) {
    if (routing.historyMode == ComposeHistoryMode.Memory) {
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
    fallback: () -> Unit,
) {
    if (routing.historyMode == ComposeHistoryMode.Memory) {
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
                withTimeout(1_000) {
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

    resetOnPopStateEvent(routing)
}

@Composable
internal actual fun Routing.restoreState(startUri: String) {
    LaunchedEffect(Unit) {
        window.onpageshow = {
            // First time or page refresh we try continue from last state
            val state = window.history.state
            when {
                state != null -> tryNotifyTheRoute(state = state)
                else -> replace(path = startUri)
            }
        }

        resetOnPopStateEvent(this@restoreState)
    }
}

internal fun ApplicationCall.uriToAddressBar(): String {
    return when {
        application.historyMode != ComposeHistoryMode.Hash -> uri

        else -> HASH_PREFIX + uri
    }
}

private fun resetOnPopStateEvent(routing: Routing) {
    window.onpopstate = { event ->
        routing.tryNotifyTheRoute(state = event.state)
    }
}

private fun Routing.tryNotifyTheRoute(state: Any?) {
    val composeHistoryState = state.deserialize() ?: return
    val call = composeHistoryState.toCall(application)
    call.neglect = true
    call.popResult = popResult
    execute(call)
}
