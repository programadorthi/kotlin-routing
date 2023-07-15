package dev.programadorthi.routing.core.application

import io.ktor.events.EventDefinition

/**
 * Event definition for Application Starting event
 *
 * Note, that application itself cannot receive this event because it fires before application is created
 * It is meant to be used by engines.
 */
public val ApplicationStarting: EventDefinition<Application> = EventDefinition()

/**
 * Event definition for Application Started event
 */
public val ApplicationStarted: EventDefinition<Application> = EventDefinition()

/**
 * Event definition for an event that is fired when the application is going to stop
 */
public val ApplicationStopPreparing: EventDefinition<ApplicationEnvironment> = EventDefinition()

/**
 * Event definition for Application Stopping event
 */
public val ApplicationStopping: EventDefinition<Application> = EventDefinition()

/**
 * Event definition for Application Stopped event
 */
public val ApplicationStopped: EventDefinition<Application> = EventDefinition()
