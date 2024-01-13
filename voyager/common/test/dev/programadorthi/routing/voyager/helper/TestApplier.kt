package dev.programadorthi.routing.voyager.helper

import androidx.compose.runtime.Applier

internal class TestApplier : Applier<Unit> {
    override val current: Unit
        get() = Unit

    override fun down(node: Unit) {}

    override fun up() {}

    override fun insertTopDown(
        index: Int,
        instance: Unit,
    ) {}

    override fun insertBottomUp(
        index: Int,
        instance: Unit,
    ) {}

    override fun remove(
        index: Int,
        count: Int,
    ) {}

    override fun move(
        from: Int,
        to: Int,
        count: Int,
    ) {}

    override fun clear() {}
}
