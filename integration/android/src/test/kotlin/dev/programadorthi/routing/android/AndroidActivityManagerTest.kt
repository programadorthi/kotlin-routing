package dev.programadorthi.routing.android

import dev.programadorthi.routing.android.fake.FakeActivityA
import dev.programadorthi.routing.android.fake.FakeActivityB
import dev.programadorthi.routing.android.fake.FakeActivityC
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
internal class AndroidActivityManagerTest {
    @Test
    fun shouldThrowExceptionWhenGettingAnNullActivity() {
        // GIVEN
        val application = RuntimeEnvironment.getApplication()
        val manager = AndroidActivityManager()
        application.registerActivityLifecycleCallbacks(manager)

        val exception =
            assertFails {
                // WHEN
                manager.currentActivity()
            }

        // THEN
        assertIs<IllegalStateException>(exception)
        assertEquals(
            "Activity manager not started. Please, install AndroidActivities plugin to your route",
            exception.message,
        )
    }

    @Test
    fun shouldNotThrowExceptionWhenGettingAnActivityFromLifecycle() {
        // GIVEN
        val application = RuntimeEnvironment.getApplication()
        val manager = AndroidActivityManager()
        application.registerActivityLifecycleCallbacks(manager)

        // WHEN
        Robolectric.buildActivity(FakeActivityA::class.java).setup()

        // THEN
        val activity = manager.currentActivity()
        assertIs<FakeActivityA>(activity)
    }

    @Test
    fun shouldLastStartedActivityBeEqualsToCurrentActivity() {
        // GIVEN
        val application = RuntimeEnvironment.getApplication()
        val manager = AndroidActivityManager()
        application.registerActivityLifecycleCallbacks(manager)

        // WHEN
        Robolectric.buildActivity(FakeActivityA::class.java).setup()
        Robolectric.buildActivity(FakeActivityB::class.java).setup()
        Robolectric.buildActivity(FakeActivityC::class.java).setup()

        // THEN
        val activity = manager.currentActivity()
        assertIs<FakeActivityC>(activity)
    }
}
