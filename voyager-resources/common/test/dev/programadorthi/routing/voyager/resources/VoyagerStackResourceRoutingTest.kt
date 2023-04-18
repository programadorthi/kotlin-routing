package dev.programadorthi.routing.voyager.resources

import dev.programadorthi.routing.core.StackRouteMethod
import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.install
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.routing
import dev.programadorthi.routing.resources.Resources
import dev.programadorthi.routing.resources.pop
import dev.programadorthi.routing.resources.push
import dev.programadorthi.routing.resources.replace
import dev.programadorthi.routing.resources.replaceAll
import dev.programadorthi.routing.voyager.VoyagerNavigator
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.resources.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class VoyagerStackResourceRoutingTest {

    @Resource("/articles")
    class Articles {
        @Resource("{id}")
        class Id(val parent: Articles = Articles(), val id: Long)
    }

    @Resource("/news")
    class News {
        @Resource("{id}")
        class Id(val parent: News = News(), val id: String)
    }

    @Test
    fun shouldPushScreenByType() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var articles: Articles? = null
        var screen: TestScreen? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(VoyagerNavigator)

            screen<Articles> { resource ->
                result = call
                articles = resource
                TestScreen(value = "typed-articles").also {
                    screen = it
                    job.complete()
                }
            }
        }

        // WHEN
        routing.push(Articles())
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertNotNull(articles)
        assertNotNull(screen)
        assertEquals("/articles", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Push, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
        assertEquals("typed-articles", screen?.value)
    }

    @Test
    fun shouldReplaceScreenByType() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var articles: Articles? = null
        var news: News? = null
        var screen: TestScreen? = null

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(VoyagerNavigator)

            screen<Articles> { resource ->
                result = call
                articles = resource
                news = null
                TestScreen(value = "typed-articles").also {
                    screen = it
                }
            }

            screen<News> { resource ->
                result = call
                articles = null
                news = resource
                TestScreen(value = "typed-news").also {
                    screen = it
                    job.complete()
                }
            }
        }

        // WHEN
        routing.push(Articles())
        advanceTimeBy(99)
        routing.replace(News())
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertNull(articles)
        assertNotNull(news)
        assertNotNull(screen)
        assertEquals("/news", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Replace, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
        assertEquals("typed-news", screen?.value)
    }

    @Test
    fun shouldReplaceAllScreenByType() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        var articles: Articles? = null
        var news: News? = null
        var screen: TestScreen? = null
        var pushCount = 0

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(VoyagerNavigator)

            screen<Articles> { resource ->
                result = call
                articles = resource
                news = null
                TestScreen(value = "typed-articles").also {
                    pushCount += 1
                    screen = it
                }
            }

            screen<News> { resource ->
                result = call
                articles = null
                news = resource
                TestScreen(value = "typed-news").also {
                    screen = it
                    if (call.routeMethod == StackRouteMethod.ReplaceAll) {
                        job.complete()
                    } else {
                        pushCount += 1
                    }
                }
            }
        }

        // WHEN
        routing.push(Articles())
        advanceTimeBy(99)
        routing.push(News())
        advanceTimeBy(99)
        routing.push(Articles())
        advanceTimeBy(99)
        routing.push(News())
        advanceTimeBy(99)
        routing.replaceAll(Articles())
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertNotNull(articles)
        assertNull(news)
        assertNotNull(screen)
        assertEquals("/articles", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.ReplaceAll, result?.routeMethod)
        assertEquals(Parameters.Empty, result?.parameters)
        assertEquals("typed-articles", screen?.value)
        assertEquals(5, pushCount)
    }

    @Test
    fun shouldPopAPushedScreenByType() = runTest {
        // GIVEN
        val job = Job()
        var result: ApplicationCall? = null
        val ids = mutableListOf<Articles.Id>()

        val routing = routing(parentCoroutineContext = coroutineContext + job) {
            install(Resources)
            install(VoyagerNavigator)

            screen<Articles.Id> {
                TestScreen(value = "typed-ids")
            }

            pop<Articles.Id> {
                result = call
                ids += it
                if (ids.size >= 3) {
                    job.complete()
                }
            }
        }

        // WHEN
        routing.push(Articles.Id(id = 1))
        advanceTimeBy(99)
        routing.push(Articles.Id(id = 2))
        advanceTimeBy(99)
        routing.push(Articles.Id(id = 3))
        advanceTimeBy(99)
        routing.pop()
        advanceTimeBy(99)
        routing.pop()
        advanceTimeBy(99)
        routing.pop()
        advanceTimeBy(99)

        // THEN
        assertNotNull(result)
        assertEquals("/articles/1", "${result?.uri}")
        assertEquals("", "${result?.name}")
        assertEquals(StackRouteMethod.Pop, result?.routeMethod)
        assertEquals(parametersOf("id", "1"), result?.parameters)
        assertEquals(listOf<Long>(3, 2, 1), ids.map { it.id })
    }
}
