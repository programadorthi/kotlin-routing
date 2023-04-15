/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package dev.programadorthi.routing.core

import io.ktor.http.Parameters
import kotlinx.coroutines.launch

public fun Routing.pop(parameters: Parameters = Parameters.Empty) {
    application.launch {
        val stackManager = application.stackManager
        execute(
            StackApplicationCall.Pop(
                application = application,
                parameters = parameters,
                uri = stackManager.last(),
            )
        )
    }
}

public fun Routing.push(path: String, parameters: Parameters = Parameters.Empty) {
    execute(
        StackApplicationCall.Push(
            application = application,
            uri = path,
            parameters = parameters,
        )
    )
}

public fun Routing.pushNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    execute(
        StackApplicationCall.PushNamed(
            application = application,
            name = name,
            parameters = parameters,
        )
    )
}

public fun Routing.replace(path: String, parameters: Parameters = Parameters.Empty) {
    execute(
        StackApplicationCall.Replace(
            application = application,
            uri = path,
            parameters = parameters,
            all = false,
        )
    )
}

public fun Routing.replaceAll(path: String, parameters: Parameters = Parameters.Empty) {
    execute(
        StackApplicationCall.Replace(
            application = application,
            uri = path,
            parameters = parameters,
            all = true,
        )
    )
}

public fun Routing.replaceNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    execute(
        StackApplicationCall.ReplaceNamed(
            application = application,
            name = name,
            parameters = parameters,
            all = false,
        )
    )
}

public fun Routing.replaceAllNamed(
    name: String,
    parameters: Parameters = Parameters.Empty,
) {
    execute(
        StackApplicationCall.ReplaceNamed(
            application = application,
            name = name,
            parameters = parameters,
            all = true,
        )
    )
}
