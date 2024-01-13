package dev.programadorthi.routing.voyager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorContent
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.OnBackPressed
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.routing
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public val LocalRouting: ProvidableCompositionLocal<Routing> =
    staticCompositionLocalOf {
        error("Composition local LocalRouting not found")
    }

@Composable
public fun VoyagerRouting(
    routing: Routing,
    initialScreen: Screen,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true },
    key: String = compositionUniqueId(),
    content: NavigatorContent = { CurrentScreen() },
) {
    CompositionLocalProvider(LocalRouting provides routing) {
        Navigator(
            screen = initialScreen,
            disposeBehavior = disposeBehavior,
            onBackPressed = onBackPressed,
            key = key,
        ) { navigator ->
            SideEffect {
                routing.application.voyagerNavigator = navigator
            }
            content(navigator)
        }
    }
}

@Composable
public fun VoyagerRouting(
    initialScreen: Screen,
    configuration: Route.() -> Unit,
    rootPath: String = "/",
    parent: Routing? = null,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    log: Logger = KtorSimpleLogger("kotlin-routing"),
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
                parentCoroutineContext = coroutineContext,
                log = log,
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
