/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.programadorthi.routing.statuspages

import kotlin.reflect.KClass

internal actual fun selectNearestParentClass(cause: Throwable, keys: List<KClass<*>>): KClass<*>? {
    if (keys.firstOrNull { cause::class == it } != null) return cause::class

    return keys.last()
}
