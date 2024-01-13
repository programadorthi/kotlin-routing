package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
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
public fun Routing(
    routing: Routing,
    initial: Content,
) {
    CompositionLocalProvider(LocalRouting provides routing) {
        val composable by remember(routing) {
            mutableStateOf(initial).also {
                routing.application.contentState = it
            }
        }

        composable()
    }
}

@Composable
public fun Routing(
    rootPath: String = "/",
    parent: Routing? = null,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    log: Logger = KtorSimpleLogger("kotlin-routing"),
    developmentMode: Boolean = false,
    configuration: Route.() -> Unit,
    initial: Content,
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

    Routing(
        routing = routing,
        initial = initial,
    )
}
