package dev.programadorthi.routing.core

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.savedstate.SavedStateRegistry

internal class StackSavedStateProvider(
    private val providerId: String,
    private val stackManager: StackManager,
) : SavedStateRegistry.SavedStateProvider {
    override fun saveState(): Bundle {
        val json = stackManager.toSave()
        return bundleOf(providerId to json)
    }

    fun restoreState(previousState: Bundle) {
        val json = previousState.getString(providerId)
        stackManager.toRestore(json)
    }
}
