package dev.programadorthi.routing.voyager.model

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

object ViewModel {
    private val random = Random.Default

    val colors = List(size = 100) {
        Color(
            red = random.nextFloat(),
            green = random.nextFloat(),
            blue = random.nextFloat(),
        )
    }

    val signedIn = mutableStateOf(false)

    val cart = mutableStateMapOf<Int, Color>()
}