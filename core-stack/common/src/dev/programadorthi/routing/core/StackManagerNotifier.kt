package dev.programadorthi.routing.core

// A routing {} with a StackManager can be created anytime.
// So we need notify for register or unregister
internal interface StackManagerNotifier {
    fun onRegistered(providerId: String, stackManager: StackManager)
    fun onUnRegistered(providerId: String)
}
