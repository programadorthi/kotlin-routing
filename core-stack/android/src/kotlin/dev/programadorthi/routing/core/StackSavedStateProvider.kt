package dev.programadorthi.routing.core

import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.savedstate.SavedStateRegistry
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.parametersOf
import io.ktor.util.toMap

internal class StackSavedStateProvider(
    private val providerId: String,
    private val stackManager: StackManager
) : SavedStateRegistry.SavedStateProvider {

    override fun saveState(): Bundle {
        val calls = stackManager.toSave()
        val parcelables = calls.map { call ->
            StackApplicationCallParcelable(
                name = call.name,
                routeMethod = call.routeMethod.value,
                uri = call.uri,
                parameters = call.parameters.toMap(),
            )
        }.toTypedArray()
        return bundleOf(providerId to parcelables)
    }

    fun restoreState(previousState: Bundle) {
        val parcelables = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            previousState
                .getParcelableArray(providerId, StackApplicationCallParcelable::class.java)
                ?.toList() ?: emptyList()
        } else {
            buildList<StackApplicationCallParcelable> {
                previousState.getParcelableArray(providerId)?.forEach { parcelable ->
                    if (parcelable is StackApplicationCallParcelable) {
                        add(parcelable)
                    }
                }
            }
        }
        val calls = parcelables.map { parcelable ->
            ApplicationCall(
                application = stackManager.application,
                name = parcelable.name,
                uri = parcelable.uri,
                routeMethod = StackRouteMethod.parse(parcelable.routeMethod),
                parameters = parametersOf(parcelable.parameters),
            )
        }
        stackManager.toRestore(calls)
    }
}
