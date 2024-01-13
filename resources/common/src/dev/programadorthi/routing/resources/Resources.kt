/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.resources

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.BaseApplicationPlugin
import dev.programadorthi.routing.core.application.plugin
import io.ktor.http.URLBuilder
import io.ktor.resources.href
import io.ktor.util.AttributeKey
import io.ktor.resources.Resources as ResourcesCore

/**
 * Adds support for type-safe routing using [ResourcesCore].
 *
 * Example:
 * ```kotlin
 * @Resource("/users")
 * class Users {
 *   @Resource("/{id}")
 *   class ById(val parent: Users = Users(), val id: Long)
 *
 *   @Resource("/add")
 *   class Add(val parent: Users = Users(), val name: String)
 * }
 *
 * routing {
 *   get<Users.ById> { userById ->
 *     val userId: Long = userById.id
 *   }
 *   post<Users.Add> { addUser ->
 *     val userName: String = addUser.name
 *   }
 * }
 *
 * // client-side
 * val newUserId = client.post(Users.Add("new_user"))
 * val addedUser = client.get(Users.ById(newUserId))
 * ```
 *
 * Server: [Type-safe routing](https://ktor.io/docs/type-safe-routing.html)
 *
 * Client: [Type-safe requests](https://ktor.io/docs/type-safe-request.html)
 *
 * @see Resource
 */
public object Resources : BaseApplicationPlugin<Application, ResourcesCore.Configuration, ResourcesCore> {
    override val key: AttributeKey<ResourcesCore> = AttributeKey("Resources")

    override fun install(
        pipeline: Application,
        configure: ResourcesCore.Configuration.() -> Unit,
    ): ResourcesCore {
        val configuration = ResourcesCore.Configuration().apply(configure)
        return ResourcesCore(configuration)
    }
}

/**
 * Constructs a URL for [resource].
 *
 * The class of the [resource] instance **must** be annotated with [Resource].
 */
public inline fun <reified T : Any> Application.href(resource: T): String {
    return href(plugin(Resources).resourcesFormat, resource)
}

/**
 * Constructs a URL for [resource].
 *
 * The class of the [resource] instance **must** be annotated with [Resource].
 */
public inline fun <reified T : Any> Application.href(
    resource: T,
    urlBuilder: URLBuilder,
) {
    href(plugin(Resources).resourcesFormat, resource, urlBuilder)
}
