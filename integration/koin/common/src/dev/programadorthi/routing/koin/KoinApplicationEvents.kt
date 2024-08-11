/*
 * Copyright Koin the original author or authors.
 * https://github.com/InsertKoinIO/koin/blob/main/projects/ktor/koin-ktor/src/main/kotlin/org/koin/ktor/plugin/KoinApplicationEvents.kt
 */
package dev.programadorthi.routing.koin

import io.ktor.events.EventDefinition
import org.koin.core.KoinApplication

/*
 * @author Arnaud Giuliani
 * @author Victor Alenkov
 */

/**
 * Event definition for [KoinApplication] Started event
 */
public val KoinApplicationStarted: EventDefinition<KoinApplication> = EventDefinition()

/**
 * Event definition for an event that is fired when the [KoinApplication] is going to stop
 */
public val KoinApplicationStopPreparing: EventDefinition<KoinApplication> = EventDefinition()

/**
 * Event definition for [KoinApplication] Stopping event
 */
public val KoinApplicationStopped: EventDefinition<KoinApplication> = EventDefinition()
