package dev.programadorthi.routing.core

import io.ktor.http.Parameters
import kotlinx.coroutines.launch

public fun Routing.pop(
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    application.launch {
        val stackManager = application.stackManager
        execute(
            StackApplicationCall.Pop(
                application = application,
                parameters = parameters,
                uri = stackManager.last(),
            ).tryNeglect(neglect)
        )
    }
}

public fun Routing.push(
    path: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    execute(
        StackApplicationCall.Push(
            application = application,
            uri = path,
            parameters = parameters,
        ).tryNeglect(neglect)
    )
}

public fun Routing.pushNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    execute(
        StackApplicationCall.PushNamed(
            application = application,
            name = name,
            parameters = parameters,
        ).tryNeglect(neglect)
    )
}

public fun Routing.replace(
    path: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    execute(
        StackApplicationCall.Replace(
            application = application,
            uri = path,
            parameters = parameters,
            all = false,
        ).tryNeglect(neglect)
    )
}

public fun Routing.replaceAll(
    path: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    execute(
        StackApplicationCall.Replace(
            application = application,
            uri = path,
            parameters = parameters,
            all = true,
        ).tryNeglect(neglect)
    )
}

public fun Routing.replaceNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    execute(
        StackApplicationCall.ReplaceNamed(
            application = application,
            name = name,
            parameters = parameters,
            all = false,
        ).tryNeglect(neglect)
    )
}

public fun Routing.replaceAllNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
    neglect: Boolean = false,
) {
    execute(
        StackApplicationCall.ReplaceNamed(
            application = application,
            name = name,
            parameters = parameters,
            all = true,
        ).tryNeglect(neglect)
    )
}

private fun StackApplicationCall.tryNeglect(neglect: Boolean): StackApplicationCall {
    stackNeglect = neglect
    return this
}
