package dev.programadorthi.routing.core

internal class FakeStackManagerNotifier : StackManagerNotifier {
    val subscriptions = mutableMapOf<String, StackManager>()
    var restoration: String? = null

    override fun onRegistered(providerId: String, stackManager: StackManager) {
        subscriptions += providerId to stackManager
        // Simulating any platform restoration after register
        stackManager.toRestore(restoration)
    }

    override fun onUnRegistered(providerId: String) {
        subscriptions -= providerId
    }
}
