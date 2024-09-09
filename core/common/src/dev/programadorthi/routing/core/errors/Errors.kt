/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.core.errors

import io.ktor.util.internal.initCauseBridge
import kotlinx.coroutines.CopyableThrowable
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Base exception to indicate that the request is not correct due to
 * wrong/missing request parameters, body content or header values.
 */
public open class BadRequestException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * This exception is thrown when a required parameter with name [parameterName] is missing
 * @property parameterName of missing request parameter
 */
@OptIn(ExperimentalCoroutinesApi::class)
public class MissingRequestParameterException(
    public val parameterName: String,
    message: String? = null,
) : BadRequestException(message ?: "Request parameter $parameterName is missing"),
    CopyableThrowable<MissingRequestParameterException> {
        override fun createCopy(): MissingRequestParameterException =
            MissingRequestParameterException(parameterName, message).also {
                it.initCauseBridge(this)
            }
    }

/**
 * URL decoder exception
 */
public class URLDecodeException(message: String) : Exception(message)

public class RouteNotFoundException(
    public override val message: String,
    public override val cause: Throwable? = null,
) : BadRequestException(message, cause)
