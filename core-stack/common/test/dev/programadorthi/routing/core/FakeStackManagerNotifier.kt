package dev.programadorthi.routing.core

internal class FakeStackManagerNotifier : StackManagerNotifier {
    val callsToRestore = mutableListOf<StackApplicationCall>()
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
