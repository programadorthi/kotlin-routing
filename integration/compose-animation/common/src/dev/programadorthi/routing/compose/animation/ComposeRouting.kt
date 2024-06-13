package dev.programadorthi.routing.compose.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import dev.programadorthi.routing.compose.CallContent
import dev.programadorthi.routing.compose.Routing
import dev.programadorthi.routing.compose.history.ComposeHistoryMode
import dev.programadorthi.routing.compose.history.restored
import dev.programadorthi.routing.compose.popped
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.routing
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
public fun Routing(
    historyMode: ComposeHistoryMode = ComposeHistoryMode.Memory,
    routing: Routing,
    startUri: String,
    enterTransition: Animation<EnterTransition>,
    exitTransition: Animation<ExitTransition>,
    popEnterTransition: Animation<EnterTransition> = enterTransition,
    popExitTransition: Animation<ExitTransition> = exitTransition,
) {
    Routing(
        historyMode = historyMode,
        routing = routing,
        startUri = startUri,
    ) { call ->
        AnimatedContent(
            targetState = call,
            transitionSpec = {
                if (nextCall.restored) {
                    fadeIn() togetherWith fadeOut()
                } else if (previousCall.popped) {
                    val popEnter = nextCall.popEnterTransition ?: popEnterTransition
                    val popExit = previousCall.popExitTransition ?: popExitTransition
                    popEnter(this) togetherWith popExit(this)
                } else {
                    val enter = nextCall.enterTransition ?: enterTransition
                    val exit = previousCall.exitTransition ?: exitTransition
                    enter(this) togetherWith exit(this)
                }
            },
            content = { animatedCall ->
                CallContent(animatedCall)
            },
        )
    }
}

@Composable
public fun Routing(
    rootPath: String = "/",
    parent: Routing? = null,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    log: Logger = KtorSimpleLogger("kotlin-routing"),
    developmentMode: Boolean = false,
    startUri: String,
    enterTransition: Animation<EnterTransition>,
    exitTransition: Animation<ExitTransition>,
    popEnterTransition: Animation<EnterTransition> = enterTransition,
    popExitTransition: Animation<ExitTransition> = exitTransition,
    configuration: Route.() -> Unit,
) {
    val routing =
        remember {
            routing(
                rootPath = rootPath,
                parent = parent,
                parentCoroutineContext = coroutineContext,
                logger = log,
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
        startUri = startUri,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
    )
}
