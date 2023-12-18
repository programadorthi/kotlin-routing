package dev.programadorthi.routing.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class StackApplicationCallParcelable(
    val name: String,
    val routeMethod: String,
    val uri: String,
    val parameters: Map<String, List<String>>,
) : Parcelable
