package dev.programadorthi.routing.core

import dev.programadorthi.routing.core.application.ApplicationCall

internal class FakeStackManagerNotifier : StackManagerNotifier {
    val callsToRestore = mutableListOf<ApplicationCall>()
    val subscriptions = mutableMapOf<String, StackManager>()

    override fun onRegistered(providerId: String, stackManager: StackManager) {
        subscriptions += providerId to stackManager
        // Simulating Android restoration after register
        stackManager.toRestore(callsToRestore)
    }

    override fun onUnRegistered(providerId: String) {
        subscriptions -= providerId
    }
}
