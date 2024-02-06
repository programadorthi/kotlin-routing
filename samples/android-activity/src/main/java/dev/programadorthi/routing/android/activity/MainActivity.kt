package dev.programadorthi.routing.android.activity

import android.content.Intent
import android.os.Bundle
import dev.programadorthi.routing.android.activity.activity.Activity1
import dev.programadorthi.routing.android.activity.activity.BaseActivity
import dev.programadorthi.routing.android.activity.helper.setPop
import dev.programadorthi.routing.android.activity.helper.setPopWithResult
import dev.programadorthi.routing.android.activity.helper.setPush
import dev.programadorthi.routing.android.activity.helper.setPushForResult
import dev.programadorthi.routing.android.activity.helper.setReplace
import dev.programadorthi.routing.android.activity.helper.setReplaceAll
import dev.programadorthi.routing.android.activity.helper.setScreenTitle
import dev.programadorthi.routing.android.popActivity
import dev.programadorthi.routing.android.pushActivity
import dev.programadorthi.routing.android.replaceActivity
import dev.programadorthi.routing.android.replaceAllActivity

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        setScreenTitle(text = "Main Activity")

        setPush(
            text = "Push Activity 1",
            action = {
                router.pushActivity(path = Activity1.PATH)
            }
        )

        setPushForResult(
            text = "Push Activity 1 for Result",
            action = {
                router.pushActivity(path = Activity1.PATH, requestCode = REQUEST_CODE)
            }
        )

        setReplace(
            text = "Replace with Activity 1",
            action = {
                router.replaceActivity(path = Activity1.PATH)
            }
        )

        setReplaceAll(
            text = "Replace all with Activity 1",
            action = {
                router.replaceAllActivity(path = Activity1.PATH)
            }
        )

        setPop {
            router.popActivity()
        }

        setPopWithResult {
            router.popActivity(result = Intent().apply {
                putExtra(EXTRA, "extra from main activity")
            })
        }
    }
}