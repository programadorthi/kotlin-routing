package dev.programadorthi.routing.core

import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.savedstate.SavedStateRegistry
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
        val calls = parcelables.mapNotNull(::mapToCall)
        stackManager.toRestore(calls)
    }

    private fun mapToCall(parcelable: StackApplicationCallParcelable): StackApplicationCall? {
        return when (val routhMethod = StackRouteMethod.parse(parcelable.routeMethod)) {
            StackRouteMethod.Push -> mapToPush(parcelable)
            StackRouteMethod.Replace,
            StackRouteMethod.ReplaceAll -> mapToReplace(parcelable, routhMethod)

            else -> null
        }
    }

    private fun mapToPush(parcelable: StackApplicationCallParcelable): StackApplicationCall {
        return when {
            parcelable.name.isBlank() -> StackApplicationCall.Push(
                application = stackManager.application,
                uri = parcelable.uri,
                parameters = parametersOf(parcelable.parameters),
            )

            else -> StackApplicationCall.PushNamed(
                application = stackManager.application,
                name = parcelable.name,
                parameters = parametersOf(parcelable.parameters),
            )
        }
    }

    private fun mapToReplace(
        parcelable: StackApplicationCallParcelable,
        routeMethod: StackRouteMethod,
    ): StackApplicationCall {
        return when {
            parcelable.name.isBlank() -> StackApplicationCall.Replace(
                all = routeMethod == StackRouteMethod.ReplaceAll,
                application = stackManager.application,
                uri = parcelable.uri,
                parameters = parametersOf(parcelable.parameters),
            )

            else -> StackApplicationCall.ReplaceNamed(
                all = routeMethod == StackRouteMethod.ReplaceAll,
                application = stackManager.application,
                name = parcelable.name,
                parameters = parametersOf(parcelable.parameters),
            )
        }
    }
}
