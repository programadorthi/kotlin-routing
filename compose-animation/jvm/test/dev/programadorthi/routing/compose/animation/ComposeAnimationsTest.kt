package dev.programadorthi.routing.compose.animation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.routing
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class ComposeAnimationsTest {
    private val routing = routing { }

    @Test
    fun shouldApplicationCallHaveNullEnterTransitionOnYourAttributes() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        val enterTransition = applicationCall.enterTransition

        // THEN
        assertNull(enterTransition, "Application call should never starts with an enter transition")
    }

    @Test
    fun shouldApplicationCallPutAnEnterTransitionToYourAttributes() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        applicationCall.enterTransition = { fadeIn() }
        val enterTransition = applicationCall.enterTransition

        // THEN
        assertNotNull(
            enterTransition,
            "Application call should put an enter transition to your attributes",
        )
    }

    @Test
    fun shouldApplicationCallRemoveAnEnterTransitionFromYourAttributesWhenTransitionIsNull() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        applicationCall.enterTransition = { fadeIn() }
        val putEnterTransition = applicationCall.enterTransition
        applicationCall.enterTransition = null
        val removedEnterTransition = applicationCall.enterTransition

        // THEN
        assertNotNull(putEnterTransition, "Application call enter transition should be registered")
        assertNull(removedEnterTransition, "Application call enter transition should be removed")
    }

    @Test
    fun shouldApplicationCallHaveNullExitTransitionOnYourAttributes() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        val exitTransition = applicationCall.exitTransition

        // THEN
        assertNull(exitTransition, "Application call should never starts with an exit transition")
    }

    @Test
    fun shouldApplicationCallPutAnExitTransitionToYourAttributes() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        applicationCall.exitTransition = { fadeOut() }
        val exitTransition = applicationCall.exitTransition

        // THEN
        assertNotNull(
            exitTransition,
            "Application call should put an exit transition to your attributes",
        )
    }

    @Test
    fun shouldApplicationCallRemoveAnExitTransitionFromYourAttributesWhenTransitionIsNull() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        applicationCall.exitTransition = { fadeOut() }
        val putExitTransition = applicationCall.exitTransition
        applicationCall.exitTransition = null
        val removedExitTransition = applicationCall.exitTransition

        // THEN
        assertNotNull(putExitTransition, "Application call exit transition should be registered")
        assertNull(removedExitTransition, "Application call exit transition should be removed")
    }

    @Test
    fun shouldApplicationCallHaveNullPopEnterTransitionOnYourAttributes() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        val popEnterTransition = applicationCall.popEnterTransition

        // THEN
        assertNull(
            popEnterTransition,
            "Application call should never starts with a pop enter transition",
        )
    }

    @Test
    fun shouldApplicationCallPutAPopEnterTransitionToYourAttributes() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        applicationCall.popEnterTransition = { fadeIn() }
        val popEnterTransition = applicationCall.popEnterTransition

        // THEN
        assertNotNull(
            popEnterTransition,
            "Application call should put a pop enter transition to your attributes",
        )
    }

    @Test
    fun shouldApplicationCallRemoveAPopEnterTransitionFromYourAttributesWhenTransitionIsNull() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        applicationCall.popEnterTransition = { fadeIn() }
        val putPopEnterTransition = applicationCall.popEnterTransition
        applicationCall.popEnterTransition = null
        val removedPopEnterTransition = applicationCall.popEnterTransition

        // THEN
        assertNotNull(
            putPopEnterTransition,
            "Application call pop enter transition should be registered",
        )
        assertNull(
            removedPopEnterTransition,
            "Application call pop enter transition should be removed",
        )
    }

    @Test
    fun shouldApplicationCallHaveNullPopExitTransitionOnYourAttributes() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        val popExitTransition = applicationCall.popExitTransition

        // THEN
        assertNull(
            popExitTransition,
            "Application call should never starts with a pop exit transition",
        )
    }

    @Test
    fun shouldApplicationCallPutAPopExitTransitionToYourAttributes() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        applicationCall.popExitTransition = { fadeOut() }
        val popExitTransition = applicationCall.popExitTransition

        // THEN
        assertNotNull(
            popExitTransition,
            "Application call should put a pop exit transition to your attributes",
        )
    }

    @Test
    fun shouldApplicationCallRemoveAPopExitTransitionFromYourAttributesWhenTransitionIsNull() {
        // GIVEN
        val applicationCall = ApplicationCall(application = routing.application, uri = "/path")

        // WHEN
        applicationCall.popExitTransition = { fadeOut() }
        val putPopExitTransition = applicationCall.popExitTransition
        applicationCall.popExitTransition = null
        val removedPopExitTransition = applicationCall.popExitTransition

        // THEN
        assertNotNull(
            putPopExitTransition,
            "Application call pop exit transition should be registered",
        )
        assertNull(
            removedPopExitTransition,
            "Application call pop exit transition should be removed",
        )
    }
}
