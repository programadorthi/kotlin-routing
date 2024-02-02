package dev.programadorthi.routing.android

import android.app.Activity
import android.os.Bundle
import dev.programadorthi.routing.core.application.ApplicationCall
import io.ktor.util.AttributeKey

private val AndroidActivityOptionsAttributeKey: AttributeKey<Bundle> =
    AttributeKey("AndroidActivityOptionsAttributeKey")

private val AndroidRequestCodeAttributeKey: AttributeKey<Int> =
    AttributeKey("AndroidRequestCodeAttributeKey")

@PublishedApi
internal var ApplicationCall.activityOptions: Bundle?
    get() = attributes.getOrNull(AndroidActivityOptionsAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(AndroidActivityOptionsAttributeKey, value)
        } else {
            attributes.remove(AndroidActivityOptionsAttributeKey)
        }
    }

@PublishedApi
internal var ApplicationCall.requestCode: Int?
    get() = attributes.getOrNull(AndroidRequestCodeAttributeKey)
    set(value) {
        if (value != null) {
            attributes.put(AndroidRequestCodeAttributeKey, value)
        } else {
            attributes.remove(AndroidRequestCodeAttributeKey)
        }
    }

public val ApplicationCall.currentActivity: Activity
    get() = activityManager.currentActivity()
