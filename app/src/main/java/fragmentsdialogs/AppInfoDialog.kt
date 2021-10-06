package fragmentsdialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.ukromap.R

class AppInfoDialog(var context: Activity) : DialogFragment() {

    @SuppressLint("ResourceType")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        var builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle("Про додаток")
        builder.setView(R.layout.launch_dialog)
        builder.setPositiveButton("Приступимо"){ _,_ -> }

        return builder.create()
    }

}