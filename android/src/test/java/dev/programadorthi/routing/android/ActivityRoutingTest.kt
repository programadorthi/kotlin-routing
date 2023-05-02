package dev.programadorthi.routing.android

import android.content.Context
import android.content.Intent
import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.StackRouting
import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.pushNamed
import dev.programadorthi.routing.core.routing
import io.ktor.http.parametersOf
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ActivityRoutingTest {

    private lateinit var applicationContext: Context
    private lateinit var fakeManager: FakeActivityManager

    @Before
    fun beforeEachTest() {
        fakeManager = FakeActivityManager()

        // MainActivity is started here simulating a Launch Activity
        val application = RuntimeEnvironment.getApplication()
        val mainActivityClass = MainActivity::class
        fakeManager.activityClassToStart = mainActivityClass
        fakeManager.start(Intent(application, mainActivityClass.java))

        applicationContext = application
    }

    @Test
    fun shouldPushAPathAndStartAnActivityManually() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var startedIntent: Intent? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(StackRouting)

            push(path = "/main", name = "main") {
                val intent = Intent(fakeManager.currentActivity, HomeActivity::class.java)
                call.parameters.forEach { key, values ->
                    if (values.size <= 1) {
                        intent.putExtra(key, values.firstOrNull() ?: "")
                    } else {
                        intent.putExtra(key, values.toTypedArray())
                    }
                }
                fakeManager.currentActivity?.startActivity(intent)
                result = call
                startedIntent = intent
                job.complete()
            }
        }

        // WHEN
        routing.push(path = "/main", parameters = parametersOf("id", "123"))
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/main", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(parametersOf("id", "123"), result?.parameters)

        val actual = shadowOf(RuntimeEnvironment.getApplication()).nextStartedActivity
        assertEquals(startedIntent?.component, actual.component)
        assertEquals(startedIntent?.getStringExtra("id"), actual.getStringExtra("id"))
    }

    @Test
    fun shouldPushAPathAndStartAnActivityAutomatically() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var startedIntent: Intent? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(AndroidNavigator) {
                context = applicationContext
            }

            application.androidNavigatorManager.changeActivityManager(fakeManager)

            push<HomeActivity>(path = "/main", name = "main") { intent ->
                result = call
                startedIntent = intent
                job.complete()
            }
        }

        // WHEN
        fakeManager.activityClassToStart = HomeActivity::class
        routing.push(path = "/main", parameters = parametersOf("id", "123"))
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/main", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(parametersOf("id", "123"), result?.parameters)

        val actual = fakeManager.intents.last()
        assertEquals(startedIntent?.component, actual.component)
        assertEquals(startedIntent?.getStringExtra("id"), actual.getStringExtra("id"))
    }

    @Test
    fun shouldPushAPathAndStartAnActivityShortVersion() = runTest {
        // GIVEN
        val job = Job()

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(AndroidNavigator) {
                context = applicationContext
            }

            application.androidNavigatorManager.changeActivityManager(fakeManager)

            push<HomeActivity>(path = "/main", name = "main")
        }

        // WHEN
        fakeManager.activityClassToStart = HomeActivity::class
        routing.pushNamed(name = "main", parameters = parametersOf("id", "123"))
        advanceTimeBy(99)

        // THEN
        val intent = fakeManager.intents.first()
        assertContains("${intent.component}", "HomeActivity")
        assertEquals("123", intent.getStringExtra("id"))
    }

    @Test
    fun shouldPushAnActivityUsingActivityClass() = runTest {
        // GIVEN
        val job = Job()

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(AndroidNavigator) {
                context = applicationContext
            }

            application.androidNavigatorManager.changeActivityManager(fakeManager)

            push<HomeActivity>(path = "/main", name = "main")
        }

        // WHEN
        fakeManager.activityClassToStart = HomeActivity::class
        routing.pushActivity<HomeActivity>()
        advanceTimeBy(99)

        // THEN
        assertContains("${fakeManager.intents.first()}", "HomeActivity")
    }

    @Test
    fun shouldRegisterAnActivityUsingActivityClass() = runTest {
        // GIVEN
        val job = Job()

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(AndroidNavigator) {
                context = applicationContext
            }

            application.androidNavigatorManager.changeActivityManager(fakeManager)

            push<HomeActivity>()
        }

        // WHEN
        fakeManager.activityClassToStart = HomeActivity::class
        routing.pushActivity<HomeActivity>()
        advanceTimeBy(99)

        // THEN
        assertContains("${fakeManager.intents.first()}", "HomeActivity")
    }

    @Test
    fun shouldReplaceActivity() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var replacedIntent: Intent? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(AndroidNavigator) {
                context = applicationContext
            }

            application.androidNavigatorManager.changeActivityManager(fakeManager)

            push<HomeActivity>(path = "/main", name = "main")

            replace<ListActivity>(path = "/list", name = "list") { intent ->
                result = call
                replacedIntent = intent
                job.complete()
            }
        }

        // WHEN
        fakeManager.activityClassToStart = HomeActivity::class
        routing.pushActivity<HomeActivity>()
        advanceTimeBy(99)

        fakeManager.activityClassToStart = ListActivity::class
        routing.replaceActivity<ListActivity>()
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/list", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Replace, result?.routeMethod)
        assertEquals(parametersOf(), result?.parameters)

        val intent = fakeManager.intents.last()
        assertContains("${intent.component}", "ListActivity")
        assertEquals(intent, replacedIntent)
    }

    @Test
    fun shouldReplaceAllActivities() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var replacedIntent: Intent? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(AndroidNavigator) {
                context = applicationContext
            }

            application.androidNavigatorManager.changeActivityManager(fakeManager)

            push<HomeActivity>(path = "/main", name = "main")

            replaceAll<ListActivity>(path = "/list", name = "list") { intent ->
                result = call
                replacedIntent = intent
                job.complete()
            }
        }

        // WHEN
        fakeManager.activityClassToStart = HomeActivity::class
        routing.pushActivity<HomeActivity>()
        advanceTimeBy(99)

        fakeManager.activityClassToStart = ListActivity::class
        routing.replaceAllActivity<ListActivity>()
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/list", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.ReplaceAll, result?.routeMethod)
        assertEquals(parametersOf(), result?.parameters)

        assertEquals(fakeManager.intents.last(), replacedIntent)
    }

    @Test
    fun shouldPopAnActivity() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(AndroidNavigator) {
                context = applicationContext
            }

            application.androidNavigatorManager.changeActivityManager(fakeManager)

            push<HomeActivity>(path = "/main", name = "main")

            pop<HomeActivity>(path = "/main") {
                result = call
                job.complete()
            }
        }

        // WHEN
        fakeManager.activityClassToStart = HomeActivity::class
        routing.pushActivity<HomeActivity>()
        advanceTimeBy(99)
        routing.pop()
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/main", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Pop, result?.routeMethod)
        assertEquals(parametersOf(), result?.parameters)
    }

    @Test
    fun shouldHandleAnyCallToStartAnActivity() = runTest {
        // GIVEN
        val job = Job()
        val receivedCalls = mutableListOf<ApplicationCall>()
        val receivedIntents = mutableListOf<Intent?>()

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(AndroidNavigator) {
                context = applicationContext
            }

            application.androidNavigatorManager.changeActivityManager(fakeManager)

            handle<HomeActivity> { intent ->
                receivedCalls += call
                receivedIntents += intent
            }

            handle<ListActivity> { intent ->
                receivedCalls += call
                receivedIntents += intent
            }

            handle<DetailsActivity> { intent ->
                receivedCalls += call
                receivedIntents += intent
            }
        }

        // WHEN
        fakeManager.activityClassToStart = HomeActivity::class
        routing.pushActivity<HomeActivity>()
        advanceTimeBy(99)

        fakeManager.activityClassToStart = ListActivity::class
        routing.replaceActivity<ListActivity>()
        advanceTimeBy(99)

        fakeManager.activityClassToStart = DetailsActivity::class
        routing.replaceAllActivity<DetailsActivity>()
        advanceTimeBy(99)

        routing.pop()
        advanceTimeBy(99)

        // THEN
        // push
        assertEquals("/${HomeActivity::class.qualifiedName}", receivedCalls[0].uri)
        assertEquals("", receivedCalls[0].name)
        assertEquals(StackRouteMethod.Push, receivedCalls[0].routeMethod)
        assertEquals(parametersOf(), receivedCalls[0].parameters)
        assertEquals(fakeManager.intents[0], receivedIntents[0])
        // replace
        assertEquals("/${ListActivity::class.qualifiedName}", receivedCalls[1].uri)
        assertEquals("", receivedCalls[1].name)
        assertEquals(StackRouteMethod.Replace, receivedCalls[1].routeMethod)
        assertEquals(parametersOf(), receivedCalls[1].parameters)
        assertEquals(fakeManager.intents[1], receivedIntents[1])
        // replace all
        assertEquals("/${DetailsActivity::class.qualifiedName}", receivedCalls[2].uri)
        assertEquals("", receivedCalls[2].name)
        assertEquals(StackRouteMethod.ReplaceAll, receivedCalls[2].routeMethod)
        assertEquals(parametersOf(), receivedCalls[2].parameters)
        assertEquals(fakeManager.intents[2], receivedIntents[2])
        // pop
        assertEquals("/${DetailsActivity::class.qualifiedName}", receivedCalls[3].uri)
        assertEquals("", receivedCalls[3].name)
        assertEquals(StackRouteMethod.Pop, receivedCalls[3].routeMethod)
        assertEquals(parametersOf(), receivedCalls[3].parameters)
        assertNull(receivedIntents[3])
    }
}