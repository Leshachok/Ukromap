package fragmentsdialogs

import activities.LoginActivity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_friends.*

class RestartDialog(var reload: Boolean) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        var builder = AlertDialog.Builder(context)
        builder.setTitle("Помилка")
        builder.setMessage("\nПогане інтернет-з'єднання. Спробуйте перепід'єднатись. ")
        builder.setPositiveButton("Ок"){ _, _ ->
            Log.d("dialog", "ok")
        }

        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if(!reload) return
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }



}