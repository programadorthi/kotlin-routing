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

class Activity1 : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        setScreenTitle(text = "Activity 1")

        setPush(
            text = "Push Activity 2",
            action = {
                router.pushActivity(path = Activity2.PATH)
            }
        )

        setPushForResult(
            text = "Push Activity 2 for Result",
            action = {
                router.pushActivity(path = Activity2.PATH, requestCode = REQUEST_CODE)
            }
        )

        setReplace(
            text = "Replace with Activity 2",
            action = {
                router.replaceActivity(path = Activity2.PATH)
            }
        )

        setReplaceAll(
            text = "Replace all with Activity 2",
            action = {
                router.replaceAllActivity(path = Activity2.PATH)
            }
        )

        setPop {
            router.popActivity()
        }

        setPopWithResult {
            router.popActivity(result = Intent().apply {
                putExtra(EXTRA, "extra from Activity 1")
            })
        }
    }

    companion object {
        const val PATH = "/activity1"
    }
}