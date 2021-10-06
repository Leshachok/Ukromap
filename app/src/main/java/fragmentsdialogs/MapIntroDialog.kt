package fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.fragment.app.DialogFragment
import com.example.ukromap.R


class MapIntroDialog : DialogFragment() {

    private var isShown = false

    @SuppressLint("ResourceType")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        var builder:AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle("Необхіден доступ до геоданих")

        builder.setMessage("\nВідвідуючи нові пам'ятки, ви будете отримувати бали. Для 'відвідання' потрібно зайти у круг навколо пам'ятки. ")
        builder.setPositiveButton("Добре") { _, _ ->  }
        builder.setView(R.layout.map_dialog)

        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if(!isShown) {
            getLocationPermission()
            isShown = true
        }
    }

    private fun getLocationPermission() {
        requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION) , 1)
    }

    override fun onResume() {
        if(isShown) dismiss()
        super.onResume()
    }



}