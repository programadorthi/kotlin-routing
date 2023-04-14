package dev.programadorthi.routing.voyager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VoyagerEventManagerTest {

    @Test
    fun shouldStartNavigationInIdleEvent() = runTest {
        // GIVEN
        val manager = VoyagerEventManager(coroutineContext, "")
        val result = mutableListOf<VoyagerRouteEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            manager.navigation.toList(result)
        }
        // THEN
        assertEquals(result, listOf<VoyagerRouteEvent>(VoyagerRouteEvent.Idle))
    }

    @Test
    fun shouldEmitPopEvent() = runTest {
        // GIVEN
        val manager = VoyagerEventManager(coroutineContext, "")
        val result = mutableListOf<VoyagerRouteEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            manager.navigation.toList(result)
        }
        // WHEN
        manager.pop()
        advanceTimeBy(99)
        // THEN
        assertEquals(result, listOf(VoyagerRouteEvent.Idle, VoyagerRouteEvent.Pop))
    }

    @Test
    fun shouldEmitPushEvent() = runTest {
        // GIVEN
        val manager = VoyagerEventManager(coroutineContext, "")
        val result = mutableListOf<VoyagerRouteEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            manager.navigation.toList(result)
        }
        // WHEN
        manager.push(screen = TestScreen(value = "test"))
        advanceTimeBy(99)
        // THEN
        assertEquals(result.size, 2)
        assertIs<VoyagerRouteEvent.Push>(result.last())
        val event = result.last() as VoyagerRouteEvent.Push
        assertIs<TestScreen>(event.screen)
        assertEquals(event.screen.value, "test")
    }

    @Test
    fun shouldEmitReplaceEvent() = runTest {
        // GIVEN
        val manager = VoyagerEventManager(coroutineContext, "")
        val result = mutableListOf<VoyagerRouteEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            manager.navigation.toList(result)
        }
        // WHEN
        manager.replace(screen = TestScreen(value = "test"), replaceAll = false)
        advanceTimeBy(99)
        // THEN
        assertEquals(result.size, 2)
        assertIs<VoyagerRouteEvent.Replace>(result.last())
        val event = result.last() as VoyagerRouteEvent.Replace
        assertIs<TestScreen>(event.screen)
        assertEquals(event.screen.value, "test")
        assertFalse { event.replaceAll }
    }

    @Test
    fun shouldEmitReplaceAllEvent() = runTest {
        // GIVEN
        val manager = VoyagerEventManager(coroutineContext, "")
        val result = mutableListOf<VoyagerRouteEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            manager.navigation.toList(result)
        }
        // WHEN
        manager.replace(screen = TestScreen(value = "test"), replaceAll = true)
        advanceTimeBy(99)
        // THEN
        assertEquals(result.size, 2)
        assertIs<VoyagerRouteEvent.Replace>(result.last())
        val event = result.last() as VoyagerRouteEvent.Replace
        assertIs<TestScreen>(event.screen)
        assertEquals(event.screen.value, "test")
        assertTrue { event.replaceAll }
    }

    @Test
    fun shouldBackToIdleWhenClearEvent() = runTest {
        // GIVEN
        val manager = VoyagerEventManager(coroutineContext, "")
        val result = mutableListOf<VoyagerRouteEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            manager.navigation.toList(result)
        }
        // WHEN
        manager.push(screen = TestScreen(value = "test"))
        advanceTimeBy(99)
        manager.clearEvent()
        advanceTimeBy(99)
        // THEN
        assertEquals(result.size, 3)
        assertIs<VoyagerRouteEvent.Idle>(result.last())
    }
}
