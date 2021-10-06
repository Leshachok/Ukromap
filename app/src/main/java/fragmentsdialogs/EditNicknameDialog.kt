package fragmentsdialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.ukromap.R

class EditNicknameDialog(var context: Activity) : DialogFragment() {

    private var REQUEST_CODE = 100
    private var REFUSE_CODE = 101
    private var INVALID_TEXT_CODE = 102

    @SuppressLint("ResourceType")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        var builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        var layoutinfaltor = context.layoutInflater
        var view = layoutinfaltor.inflate(R.layout.dialog_edit_nickname, context.findViewById(R.id.edit_nickname_container))
        var edittext = view.findViewById<EditText>(R.id.editTextTextPersonName)
        var intent = Intent()

        builder.setPositiveButton("Готово"){ _, _ ->
            var text = edittext.text.toString()
            var CODE = if(text.length < 5) INVALID_TEXT_CODE else REQUEST_CODE
            var time = System.currentTimeMillis()
            intent.putExtra("nickname", text)
            intent.putExtra("time", time)
            targetFragment!!.onActivityResult(REQUEST_CODE, CODE, intent)
        }
        builder.setNegativeButton("Назад"){_, _ ->
            targetFragment!!.onActivityResult(REQUEST_CODE, REFUSE_CODE, intent)
        }
        builder.setTitle("Зміна нікнейму")
        builder.setMessage("\nНікнейм можна змінити лише раз на добу")
        builder.setView(view)

        return builder.create()
    }
}