package dev.programadorthi.routing.voyager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorContent
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.OnBackPressed
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.voyager.history.VoyagerHistoryMode
import dev.programadorthi.routing.voyager.history.historyMode
import dev.programadorthi.routing.voyager.history.restoreState
import io.ktor.util.logging.Logger
import kotlin.coroutines.CoroutineContext

public val LocalVoyagerRouting: ProvidableCompositionLocal<Routing> =
    staticCompositionLocalOf {
        error("Composition local LocalVoyagerRouting not found")
    }

@Composable
public fun VoyagerRouting(
    historyMode: VoyagerHistoryMode = VoyagerHistoryMode.Memory,
    routing: Routing,
    initialScreen: Screen,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true },
    key: String = compositionUniqueId(),
    content: NavigatorContent = { CurrentScreen() },
) {
    CompositionLocalProvider(LocalVoyagerRouting provides routing) {
        var stateToRestore by remember { mutableStateOf<Any?>(null) }

        routing.restoreState { state ->
            stateToRestore = state
        }

        Navigator(
            screen = initialScreen,
            disposeBehavior = disposeBehavior,
            onBackPressed = onBackPressed,
            key = key,
        ) { navigator ->
            SideEffect {
                routing.voyagerNavigator = navigator
                routing.historyMode = historyMode

                if (stateToRestore != null) {
                    val call = stateToRestore as? ApplicationCall
                    val path = stateToRestore as? String ?: ""
                    when {
                        call != null -> routing.execute(call)
                        path.isNotBlank() -> routing.replace(path)
                    }
                    stateToRestore = null
                }
            }
            content(navigator)
        }
    }
}

@Composable
public fun VoyagerRouting(
    historyMode: VoyagerHistoryMode = VoyagerHistoryMode.Memory,
    initialScreen: Screen,
    configuration: Route.() -> Unit,
    rootPath: String = "/",
    parent: Routing? = null,
    parentCoroutineContext: CoroutineContext? = null,
    logger: Logger? = null,
    developmentMode: Boolean = false,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true },
    key: String = compositionUniqueId(),
    content: NavigatorContent = { CurrentScreen() },
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

    VoyagerRouting(
        historyMode = historyMode,
        routing = routing,
        initialScreen = initialScreen,
        disposeBehavior = disposeBehavior,
        onBackPressed = onBackPressed,
        key = key,
        content = content,
    )
}

@Composable
private fun compositionUniqueId(): String = currentCompositeKeyHash.toString(MAX_SUPPORTED_RADIX)

private const val MAX_SUPPORTED_RADIX = 36
