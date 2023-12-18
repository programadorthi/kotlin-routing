package dev.programadorthi.routing.core

internal class FakeStackManagerNotifier : StackManagerNotifier {
    val subscriptions = mutableMapOf<String, StackManager>()

    override fun onRegistered(providerId: String, stackManager: StackManager) {
        subscriptions += providerId to stackManager
    }

    override fun onUnRegistered(providerId: String) {
        subscriptions -= providerId
    }
}
