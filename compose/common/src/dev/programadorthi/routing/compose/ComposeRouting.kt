package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
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
public fun CurrentContent() {
    val routing = LocalRouting.current
    val lastCall = routing.callStack.last()
    lastCall.content(lastCall)
}

@Composable
public fun Routing(
    routing: Routing,
    initial: ComposeContent,
    content: ComposeContent = { CurrentContent() },
) {
    CompositionLocalProvider(LocalRouting provides routing) {
        val router =
            remember(routing) {
                val stack = mutableStateListOf<ApplicationCall>()
                val call =
                    ApplicationCall(
                        application = routing.application,
                        uri = routing.toString(),
                    )
                call.content = initial
                stack += call
                routing.callStack = stack
                routing
            }
        content(router.callStack.last())
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
    initial: ComposeContent,
    content: ComposeContent = { CurrentContent() },
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
        content = content,
    )
}
