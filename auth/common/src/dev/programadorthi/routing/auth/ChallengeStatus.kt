package dev.programadorthi.routing.auth

public sealed class ChallengeStatus {
    public object Approved : ChallengeStatus()
    public object Denied : ChallengeStatus()
    public object NotSolved : ChallengeStatus()

    internal data class Redirected(val destination: String) : ChallengeStatus()
}
