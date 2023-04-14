package dev.programadorthi.routing.voyager

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.programadorthi.routing.core.Routing

public val VoyagerLocalRouter: ProvidableCompositionLocal<Routing?> =
    staticCompositionLocalOf { null }
