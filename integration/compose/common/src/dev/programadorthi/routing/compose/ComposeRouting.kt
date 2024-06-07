package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import dev.programadorthi.routing.compose.history.ComposeHistoryMode
import dev.programadorthi.routing.compose.history.historyMode
import dev.programadorthi.routing.compose.history.restoreState
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.routing
import io.ktor.util.logging.Logger
import kotlin.coroutines.CoroutineContext

public val LocalRouting: ProvidableCompositionLocal<Routing> =
    staticCompositionLocalOf {
        error("Composition local LocalRouting not found")
    }

@Composable
public fun CallContent(call: ApplicationCall) {
    call.content?.invoke(call)
}

@Composable
public fun Routing(
    historyMode: ComposeHistoryMode = ComposeHistoryMode.Memory,
    routing: Routing,
    startUri: String,
    content: ComposeContent = { CallContent(it) },
) {
    CompositionLocalProvider(LocalRouting provides routing) {
        val stack =
            remember(routing, historyMode) {
                mutableStateListOf<ApplicationCall>()
            }
        val router =
            remember(routing, historyMode) {
                routing.callStack = stack
                routing.historyMode = historyMode
                routing
            }
        router.restoreState(startUri = startUri)
        stack.lastOrNull()?.let { call ->
            content(call)
        }
    }
}

@Composable
public fun Routing(
    historyMode: ComposeHistoryMode = ComposeHistoryMode.Memory,
    rootPath: String = "/",
    parent: Routing? = null,
    parentCoroutineContext: CoroutineContext? = null,
    logger: Logger? = null,
    developmentMode: Boolean = false,
    startUri: String,
    configuration: Route.() -> Unit,
) {
    val routing =
        remember {
            routing(
                rootPath = rootPath,
                parent = parent,
                parentCoroutineContext = parentCoroutineContext,
                logger = logger,
                developmentMode = developmentMode,
                configuration = configuration,
            )
        }

    DisposableEffect(routing) {
        onDispose {
            routing.dispose()
        }
    }

    Routing(
        historyMode = historyMode,
        routing = routing,
        startUri = startUri,
    )
}
