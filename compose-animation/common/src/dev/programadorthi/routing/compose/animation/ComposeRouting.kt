package dev.programadorthi.routing.compose.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
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
import dev.programadorthi.routing.compose.CurrentContent
import dev.programadorthi.routing.compose.Routing
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
    content: ComposeAnimatedContent = { CurrentContent() },
) {
    val routingUri =
        remember(routing) {
            routing.toString()
        }

    Routing(
        routing = routing,
        initial = { },
    ) { stateCall ->
        AnimatedContent(
            targetState = stateCall,
            transitionSpec = {
                transitionSpec(
                    scope = this,
                    enterTransition = enterTransition,
                    exitTransition = exitTransition,
                    popEnterTransition = popEnterTransition,
                    popExitTransition = popExitTransition,
                )
            },
            content = { call ->
                if (call.uri == routingUri) {
                    initial(call)
                } else {
                    content(call)
                }
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
    content: ComposeAnimatedContent = { CurrentContent() },
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
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        initial = initial,
        content = content,
    )
}

private fun transitionSpec(
    scope: AnimatedContentTransitionScope<ApplicationCall>,
    enterTransition: Animation<EnterTransition>,
    exitTransition: Animation<ExitTransition>,
    popEnterTransition: Animation<EnterTransition> = enterTransition,
    popExitTransition: Animation<ExitTransition> = exitTransition,
): ContentTransform =
    with(scope) {
        val previousEntry = initialState
        val nextEntry = targetState

        if (previousEntry.popped) {
            val enter = nextEntry.popEnterTransition ?: popEnterTransition
            val exit = previousEntry.popExitTransition ?: popExitTransition
            return@with enter(this).togetherWith(exit(this))
        }

        val enter = nextEntry.enterTransition ?: enterTransition
        val exit = previousEntry.exitTransition ?: exitTransition
        return@with enter(this).togetherWith(exit(this))
    }
