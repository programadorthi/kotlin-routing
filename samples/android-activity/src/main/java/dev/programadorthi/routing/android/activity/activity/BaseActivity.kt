package dev.programadorthi.routing.android.activity.activity

import android.app.Activity
import android.content.Intent
import android.widget.TextView
import dev.programadorthi.routing.android.activity.R

abstract class BaseActivity : Activity() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            findViewById<TextView>(R.id.screenTitle).text =
                data?.getStringExtra(EXTRA) ?: "NO RESULT"
        }
    }

    companion object {
        const val EXTRA = "EXTRA"
        const val REQUEST_CODE = 1234
    }
}