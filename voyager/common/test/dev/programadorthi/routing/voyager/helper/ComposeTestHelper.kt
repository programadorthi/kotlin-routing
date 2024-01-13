package dev.programadorthi.routing.voyager.helper

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
internal fun runComposeTest(content: TestScope.(CoroutineContext, Composition, BroadcastFrameClock) -> Unit) =
    runTest {
        val job = Job()
        val clock = BroadcastFrameClock()
        val scope = CoroutineScope(coroutineContext + job + clock)
        val recomposer = Recomposer(scope.coroutineContext)
        val runner =
            scope.launch {
                recomposer.runRecomposeAndApplyChanges()
            }
        val composition = Composition(TestApplier(), recomposer)
        try {
            content(scope.coroutineContext, composition, clock)
        } finally {
            runner.cancel()
            recomposer.close()
            job.cancel()
        }
    }
