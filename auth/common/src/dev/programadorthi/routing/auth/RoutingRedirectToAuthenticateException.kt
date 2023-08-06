package dev.programadorthi.routing.auth

import dev.programadorthi.routing.core.errors.BadRequestException

public class RoutingRedirectToAuthenticateException(message: String) : BadRequestException(message)
