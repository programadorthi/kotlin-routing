package dev.programadorthi.routing.auth

import dev.programadorthi.routing.core.errors.BadRequestException

public class RoutingUnauthorizedException(
    message: String,
) : BadRequestException(message)
