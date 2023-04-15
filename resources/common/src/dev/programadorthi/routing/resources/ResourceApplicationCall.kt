package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.RouteMethod
import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.util.Attributes

public class ResourceApplicationCall(
    override val application: Application,
    override val uri: String,
) : ApplicationCall {

    override val attributes: Attributes = Attributes()

    override val routeMethod: RouteMethod get() = RouteMethod.Empty

    override val name: String get() = ""

    override val parameters: Parameters get() = Parameters.Empty
}
