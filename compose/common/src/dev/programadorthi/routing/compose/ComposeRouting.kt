package dev.programadorthi.routing.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.programadorthi.routing.core.Routing
import dev.programadorthi.routing.core.application

@Composable
public fun Routing(
    routing: Routing,
    initial: Content,
) {
    val composable by remember(routing) {
        mutableStateOf(initial).also {
            routing.application.contentState = it
        }
    }

    composable()
}
