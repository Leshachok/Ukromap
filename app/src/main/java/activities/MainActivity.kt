package activities

import android.Manifest
import android.R.attr.data
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import architecture.viewmodels.MainViewModel
import com.beust.klaxon.Klaxon
import com.example.ukromap.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import fragments.AccountFragment
import fragments.FriendsFragment
import fragments.MapFragment
import fragments.MapFragment.Companion.locationPermissionGranted
import fragments.MapIntroDialog
import kotlinx.android.synthetic.main.activity_main.*
import util.RestartManager
import java.lang.ClassCastException
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel:MainViewModel

    private val onNavigationListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.account -> {
                    replaceFragment(AccountFragment())
                    true
                }
                R.id.map -> {
                    replaceFragment(MapFragment())
                    launchDialog()
                    true
                }
                R.id.friends -> {
                    replaceFragment(FriendsFragment())
                    true
                }
                else -> {
                    Toast.makeText(this, "error", Toast.LENGTH_SHORT).show(); true
                }
            }
        }

    private val onNavigationResetListener =
        BottomNavigationView.OnNavigationItemReselectedListener { false}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation.setOnNavigationItemSelectedListener(onNavigationListener)
        bottomNavigation.setOnNavigationItemReselectedListener(onNavigationResetListener)

        initViewModel()
        setIconsStyle()

        replaceFragment(AccountFragment())
    }

    override fun onResume() {
        super.onResume()
        RestartManager.fragmentManager = supportFragmentManager
    }

    private fun initViewModel() {
        mainViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
        ).get(MainViewModel::class.java)
    }

    private fun setIconsStyle(){
        val iconsColorStates = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                Color.parseColor("#139513"),
                Color.parseColor("#39E539")//выделенное
            )
        )

        val textColorStates = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                Color.parseColor("#139513"),
                Color.parseColor("#39E539")
            )
        )

        bottomNavigation.itemIconTintList = iconsColorStates
        bottomNavigation.itemTextColor = textColorStates
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("permisson", "activity + $locationPermissionGranted")
        when(requestCode){
            1 -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                    try {
                        Log.d("permisson1", "activity + $locationPermissionGranted")
                        var fragment =
                            supportFragmentManager.findFragmentById(MapFragment.id) as MapFragment?
                        fragment!!.onRequestPermissionsResult(1, permissions, grantResults)
                        Log.d("permisson2", "activity + $locationPermissionGranted")
                    }catch (e: ClassCastException){
                        Log.d("ERRROR", e.printStackTrace().toString())
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun launchDialog() {
        var preferences = this.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if(!preferences.contains("first_map_launch_dialog")){
            var editor = preferences.edit()
            editor.putBoolean("first_map_launch_dialog", true)
            editor.apply()
            var dialog = MapIntroDialog()
            dialog.show(supportFragmentManager, "permission_request")
        }else getLocationPermission()
    }


    private fun getLocationPermission(){
        if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED){
            locationPermissionGranted = true
        }else{
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

}
