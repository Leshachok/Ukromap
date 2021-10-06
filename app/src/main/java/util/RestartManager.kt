package util

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentManager
import fragmentsdialogs.RestartDialog

class RestartManager {

    companion object {

        lateinit var fragmentManager:FragmentManager

        fun restart(){
            var dialog = RestartDialog(true)
            dialog.show(fragmentManager, "restart")
        }
    }

}