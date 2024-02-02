package dev.programadorthi.routing.android

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import dev.programadorthi.routing.android.fake.FakeActivityA
import dev.programadorthi.routing.android.fake.FakeActivityB
import dev.programadorthi.routing.android.fake.FakeActivityC
import dev.programadorthi.routing.android.fake.FakeActivityManager
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.call
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class AndroidRoutingTest {
    @Test
    fun shouldStartActivityUsingGenericCall() =
        runTest {
            // GIVEN
            val application = RuntimeEnvironment.getApplication()
            val job = Job()
            val fakeActivityManager = FakeActivityManager(mainActivity = Activity::class.java)
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(AndroidActivities) {
                        context = application
                        manager = fakeActivityManager
                    }

                    activity<FakeActivityA>(path = "/fakeA")
                }

            // WHEN
            routing.call(uri = "/fakeA")
            advanceTimeBy(99) // Ask for routing

            // THEN
            val activity = fakeActivityManager.currentActivity()
            assertIs<FakeActivityA>(activity)
        }

    @Test
    fun shouldPushAnActivity() =
        runTest {
            // GIVEN
            val application = RuntimeEnvironment.getApplication()
            val job = Job()
            val fakeActivityManager = FakeActivityManager(mainActivity = Activity::class.java)
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(AndroidActivities) {
                        context = application
                        manager = fakeActivityManager
                    }

                    activity(path = "/fakeA") {
                        Intent(call.currentActivity, FakeActivityA::class.java)
                    }
                }

            // WHEN
            routing.pushActivity(path = "/fakeA")
            advanceTimeBy(99) // Ask for routing

            // THEN
            val activity = fakeActivityManager.currentActivity()
            assertIs<FakeActivityA>(activity)
        }

    @Test
    fun shouldReplaceAnActivity() =
        runTest {
            // GIVEN
            val application = RuntimeEnvironment.getApplication()
            val job = Job()
            val fakeActivityManager = FakeActivityManager(mainActivity = FakeActivityA::class.java)
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(AndroidActivities) {
                        context = application
                        manager = fakeActivityManager
                    }

                    activity<FakeActivityB>(path = "/fakeB")
                    activity<FakeActivityC>(path = "/fakeC")
                }

            // WHEN
            routing.pushActivity(path = "/fakeB")
            advanceTimeBy(99) // Ask for routing
            val activityB = fakeActivityManager.currentActivity()

            // WHEN
            routing.replaceActivity(path = "/fakeC")
            advanceTimeBy(99) // Ask for routing

            // THEN
            val activityC = fakeActivityManager.currentActivity()

            assertIs<FakeActivityB>(activityB)
            assertIs<FakeActivityC>(activityC)
            assertTrue(
                activityB.isFinished,
                "Previous activity should be finished after a replace call",
            )
        }

    @Test
    fun shouldReplaceAllActivity() =
        runTest {
            // GIVEN
            val application = RuntimeEnvironment.getApplication()
            val job = Job()
            val fakeActivityManager = FakeActivityManager(mainActivity = FakeActivityA::class.java)
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(AndroidActivities) {
                        context = application
                        manager = fakeActivityManager
                    }

                    activity(path = "/fakeB") {
                        Intent(call.currentActivity, FakeActivityB::class.java)
                    }

                    activity(path = "/fakeC") {
                        Intent(call.currentActivity, FakeActivityC::class.java)
                    }
                }

            // WHEN
            routing.pushActivity(path = "/fakeB")
            advanceTimeBy(99) // Ask for routing
            val activityB = fakeActivityManager.currentActivity()

            // WHEN
            routing.replaceAllActivity(path = "/fakeC")
            advanceTimeBy(99) // Ask for routing

            // THEN
            val activityC = fakeActivityManager.currentActivity()

            assertIs<FakeActivityB>(activityB)
            assertIs<FakeActivityC>(activityC)
            assertTrue(
                activityB.isFinishedAffinity,
                "Previous activity should call finishAffinity() after a replace all call",
            )
        }

    @Test
    fun shouldStartActivityForResult() =
        runTest {
            // GIVEN
            val requestCode = 12345678
            val application = RuntimeEnvironment.getApplication()
            val job = Job()
            val fakeActivityManager = FakeActivityManager(mainActivity = FakeActivityA::class.java)
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(AndroidActivities) {
                        context = application
                        manager = fakeActivityManager
                    }

                    activity<FakeActivityB>(path = "/fakeB")
                }

            // WHEN
            routing.pushActivity(path = "/fakeB", requestCode = requestCode)
            advanceTimeBy(99) // Ask for routing
            val shadowActivity = Shadows.shadowOf(fakeActivityManager.currentActivity())

            routing.popActivity()
            advanceTimeBy(99) // Ask for routing

            // THEN
            val intentForResult = shadowActivity.nextStartedActivityForResult
            assertEquals(
                FakeActivityB::class.qualifiedName,
                intentForResult.intent?.component?.className,
            )
            assertEquals(requestCode, intentForResult.requestCode)
            assertEquals(Activity.RESULT_CANCELED, shadowActivity.resultCode)
            assertNull(
                shadowActivity.resultIntent,
                "result intent should be null after pop without a value",
            )
        }

    @Test
    fun shouldStartActivityForResultAndReceiveTheResult() =
        runTest {
            // GIVEN
            val requestCode = 12345678
            val resultData =
                Intent().apply {
                    putExtra("key", "value")
                }
            val application = RuntimeEnvironment.getApplication()
            val job = Job()
            val fakeActivityManager = FakeActivityManager(mainActivity = FakeActivityA::class.java)
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(AndroidActivities) {
                        context = application
                        manager = fakeActivityManager
                    }

                    activity<FakeActivityB>(path = "/fakeB")
                }

            // WHEN
            routing.pushActivity(path = "/fakeB", requestCode = requestCode)
            advanceTimeBy(99) // Ask for routing
            val shadowActivity = Shadows.shadowOf(fakeActivityManager.currentActivity())

            routing.popActivity(result = resultData)
            advanceTimeBy(99) // Ask for routing

            // THEN
            val intentForResult = shadowActivity.nextStartedActivityForResult
            assertEquals(
                FakeActivityB::class.qualifiedName,
                intentForResult.intent?.component?.className,
            )
            assertEquals(requestCode, intentForResult.requestCode)
            assertEquals(Activity.RESULT_OK, shadowActivity.resultCode)
            assertEquals(resultData, shadowActivity.resultIntent)
        }

    @Test
    fun shouldStartActivityWithOptions() =
        runTest {
            // GIVEN
            val application = RuntimeEnvironment.getApplication()
            val job = Job()
            val fakeActivityManager = FakeActivityManager(mainActivity = FakeActivityA::class.java)
            val routing =
                routing(parentCoroutineContext = coroutineContext + job) {
                    install(AndroidActivities) {
                        context = application
                        manager = fakeActivityManager
                    }

                    activity<FakeActivityB>(path = "/fakeB")
                }

            // WHEN
            val shadowActivity = Shadows.shadowOf(fakeActivityManager.currentActivity())

            routing.pushActivity(
                path = "/fakeB",
                activityOptions = ActivityOptions.makeBasic().toBundle(),
            )
            advanceTimeBy(99) // Ask for routing

            // THEN
            val intentForResult = shadowActivity.nextStartedActivityForResult
            assertNotNull(intentForResult.options, "Options should not be null when provided")
        }
}
