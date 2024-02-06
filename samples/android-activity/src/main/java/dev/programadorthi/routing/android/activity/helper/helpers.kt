package dev.programadorthi.routing.android.activity.helper

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import dev.programadorthi.routing.android.activity.R

internal fun Activity.setScreenTitle(text: String) {
    findViewById<TextView>(R.id.screenTitle).text = text
}

internal fun Activity.setPop(action: () -> Unit) {
    val button = findViewById<Button>(R.id.popButton)
    button.setOnClickListener { action() }
}

internal fun Activity.setPopWithResult(action: () -> Unit) {
    val button = findViewById<Button>(R.id.popWithResultButton)
    button.setOnClickListener { action() }
}

internal fun Activity.setPush(text: String, action: () -> Unit) {
    val button = findViewById<Button>(R.id.pushButton)
    button.text = text
    button.setOnClickListener { action() }
}

internal fun Activity.setPushForResult(text: String, action: () -> Unit) {
    val button = findViewById<Button>(R.id.pushForResultButton)
    button.text = text
    button.setOnClickListener { action() }
}

internal fun Activity.setReplace(text: String, action: () -> Unit) {
    val button = findViewById<Button>(R.id.replaceButton)
    button.text = text
    button.setOnClickListener { action() }
}

internal fun Activity.setReplaceAll(text: String, action: () -> Unit) {
    val button = findViewById<Button>(R.id.replaceAllButton)
    button.text = text
    button.setOnClickListener { action() }
}