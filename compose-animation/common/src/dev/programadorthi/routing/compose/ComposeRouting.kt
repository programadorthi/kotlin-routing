package dev.programadorthi.routing.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
    initial: @Composable AnimatedContentScope.() -> Unit,
) {
    CompositionLocalProvider(LocalRouting provides routing) {
        val stateList =
            remember(routing) {
                mutableStateListOf<ComposeEntry>().apply {
                    routing.contentList = this
                    add(
                        ComposeEntry(
                            content = initial,
                            call =
                                ApplicationCall(
                                    application = routing.application,
                                    uri = routing.toString(),
                                ),
                        ).apply {
                            this.enterTransition = enterTransition
                            this.exitTransition = exitTransition
                            this.popEnterTransition = popEnterTransition
                            this.popExitTransition = popExitTransition
                        },
                    )
                }
            }
        AnimatedContent(
            targetState = stateList.last(),
            transitionSpec = {
                transitionSpec(
                    scope = this,
                    enterTransition = enterTransition,
                    exitTransition = exitTransition,
                    popEnterTransition = popEnterTransition,
                    popExitTransition = popExitTransition,
                )
            },
            content = { entry ->
                entry.content(this)
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
    initial: @Composable AnimatedContentScope.() -> Unit,
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
    )
}

private fun transitionSpec(
    scope: AnimatedContentTransitionScope<ComposeEntry>,
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
