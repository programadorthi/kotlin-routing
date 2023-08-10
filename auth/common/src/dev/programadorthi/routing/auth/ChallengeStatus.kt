package dev.programadorthi.routing.auth

public sealed class ChallengeStatus {
    public object Denied : ChallengeStatus()
    public data class Approved<T : Principal>(val principal: T) : ChallengeStatus()

    internal data class Redirected(val destination: String) : ChallengeStatus()
}
