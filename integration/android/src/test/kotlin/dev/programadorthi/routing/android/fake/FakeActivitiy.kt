package dev.programadorthi.routing.android.fake

import android.app.Activity

internal abstract class BaseFakeActivity : Activity() {
    internal var isFinished = false
        private set

    internal var isFinishedAffinity = false
        private set

    override fun finishAfterTransition() {
        isFinished = true
        super.finishAfterTransition()
    }

    override fun finishAffinity() {
        isFinishedAffinity = true
        super.finishAffinity()
    }
}

internal class FakeActivityA : BaseFakeActivity()

internal class FakeActivityB : BaseFakeActivity()

internal class FakeActivityC : BaseFakeActivity()
