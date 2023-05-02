package dev.programadorthi.routing.android

import dev.programadorthi.routing.core.application
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.routing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidNavigatorPluginTest {

    @Test
    fun shouldThrowExceptionWhenGetANotInstalledAndroidNavigatorManager() {
        // GIVEN
        val router = routing { }
        val result = assertFails {
            // WHEN
            router.application.androidNavigatorManager
        }
        // THEN
        assertIs<IllegalStateException>(result)
        assertEquals(result.message, "No instance for key AttributeKey: AndroidNavigatorManager")
    }

    @Test
    fun shouldThrowExceptionWhenAContextIsNotProvided() {
        // WHEN
        val result = assertFails {
            routing {
                install(AndroidNavigator)
            }
        }
        // THEN
        assertIs<IllegalStateException>(result)
        assertEquals(
            result.message, """
        AndroidNavigator plugin needs a Context to work. Provide one calling: 
        install(AndroidNavigator) {
            context = YourContextHere
        }
    """.trimIndent()
        )
    }

    @Test
    fun shouldNotThrowExceptionWhenGetAInstalledAndroidNavigatorManager() {
        // GIVEN
        val router = routing {
            install(AndroidNavigator) {
                context = Robolectric.buildActivity(MainActivity::class.java).setup().get()
            }
        }
        // WHEN
        val result = router.application.androidNavigatorManager
        // THEN
        assertNotNull(result)
    }
}
