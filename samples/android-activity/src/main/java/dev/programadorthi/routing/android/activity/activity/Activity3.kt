package dev.programadorthi.routing.android.activity.activity

import android.content.Intent
import android.os.Bundle
import dev.programadorthi.routing.android.activity.R
import dev.programadorthi.routing.android.activity.helper.setPop
import dev.programadorthi.routing.android.activity.helper.setPopWithResult
import dev.programadorthi.routing.android.activity.helper.setPush
import dev.programadorthi.routing.android.activity.helper.setPushForResult
import dev.programadorthi.routing.android.activity.helper.setReplace
import dev.programadorthi.routing.android.activity.helper.setReplaceAll
import dev.programadorthi.routing.android.activity.helper.setScreenTitle
import dev.programadorthi.routing.android.activity.router
import dev.programadorthi.routing.android.popActivity
import dev.programadorthi.routing.android.pushActivity
import dev.programadorthi.routing.android.replaceActivity
import dev.programadorthi.routing.android.replaceAllActivity

class Activity3 : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        setScreenTitle(text = "Activity 3")

        setPush(
            text = "Push Activity 4",
            action = {
                router.pushActivity(path = Activity4.PATH)
            }
        )

        setPushForResult(
            text = "Push Activity 4 for Result",
            action = {
                router.pushActivity(path = Activity4.PATH, requestCode = REQUEST_CODE)
            }
        )

        setReplace(
            text = "Replace with Activity 4",
            action = {
                router.replaceActivity(path = Activity4.PATH)
            }
        )

        setReplaceAll(
            text = "Replace all with Activity 4",
            action = {
                router.replaceAllActivity(path = Activity4.PATH)
            }
        )

        setPop {
            router.popActivity()
        }

        setPopWithResult {
            router.popActivity(result = Intent().apply {
                putExtra(EXTRA, "extra from Activity 3")
            })
        }
    }

    companion object {
        const val PATH = "/activity3"
    }
}