package dev.programadorthi.routing.compose.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.programadorthi.routing.compose.CallContent
import dev.programadorthi.routing.compose.Routing
import dev.programadorthi.routing.compose.history.ComposeHistoryMode
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
    var restored by rememberSaveable(
        key = "state:restored:$routing",
        saver =
            Saver(
                restore = { mutableStateOf(true) },
                save = { true },
            ),
    ) {
        mutableStateOf(false)
    }
    Routing(
        historyMode = historyMode,
        routing = routing,
        startUri = startUri,
    ) { call ->
        if (restored) {
            CallContent(call)
        } else {
            AnimatedContent(
                targetState = call,
                transitionSpec = {
                    val previousCall = initialState
                    val nextCall = targetState
                    val enter =
                        when {
                            previousCall.popped -> nextCall.popEnterTransition ?: popEnterTransition
                            else -> nextCall.enterTransition ?: enterTransition
                        }
                    val exit =
                        when {
                            previousCall.popped ->
                                previousCall.popExitTransition
                                    ?: popExitTransition

                            else -> previousCall.exitTransition ?: exitTransition
                        }

                    enter(this) togetherWith exit(this)
                },
                content = { animatedCall ->
                    CallContent(animatedCall)
                },
            )
        }

        SideEffect {
            restored = false
        }
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
