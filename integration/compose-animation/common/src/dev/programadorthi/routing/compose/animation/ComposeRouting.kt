package dev.programadorthi.routing.compose.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import dev.programadorthi.routing.compose.Routing
import dev.programadorthi.routing.compose.content
import dev.programadorthi.routing.compose.popped
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.routing
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public typealias ComposeAnimatedContent = @Composable AnimatedVisibilityScope.(ApplicationCall) -> Unit

@Composable
public fun Routing(
    routing: Routing,
    enterTransition: Animation<EnterTransition> = {
        fadeIn(animationSpec = tween(220, delayMillis = 90)) +
            scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90))
    },
    exitTransition: Animation<ExitTransition> = {
        fadeOut(animationSpec = tween(90))
    },
    popEnterTransition: Animation<EnterTransition> = enterTransition,
    popExitTransition: Animation<ExitTransition> = exitTransition,
    initial: ComposeAnimatedContent,
    content: ComposeAnimatedContent = { call ->
        call.content(call)
    },
) {
    Routing(
        routing = routing,
        initial = { call ->
            call.animatedVisibilityScope?.initial(call)
        },
    ) { call ->
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
                        previousCall.popped -> previousCall.popExitTransition ?: popExitTransition
                        else -> previousCall.exitTransition ?: exitTransition
                    }

                enter(this) togetherWith exit(this)
            },
            content = { animatedCall ->
                animatedCall.animatedVisibilityScope = this

                content(animatedCall)
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
    enterTransition: Animation<EnterTransition> = {
        fadeIn(animationSpec = tween(220, delayMillis = 90)) +
            scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90))
    },
    exitTransition: Animation<ExitTransition> = {
        fadeOut(animationSpec = tween(90))
    },
    popEnterTransition: Animation<EnterTransition> = enterTransition,
    popExitTransition: Animation<ExitTransition> = exitTransition,
    configuration: Route.() -> Unit,
    initial: ComposeAnimatedContent,
    content: ComposeAnimatedContent = { call -> call.content(call) },
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
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        initial = initial,
        content = content,
    )
}
