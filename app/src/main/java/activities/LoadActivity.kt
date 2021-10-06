package activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import architecture.viewmodels.LoadViewModel
import com.example.ukromap.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.maps.MapsInitializer
import util.RestartManager
import java.util.*
import java.util.concurrent.Executors
import java.util.stream.Collectors
import kotlin.concurrent.timerTask

class LoadActivity : AppCompatActivity() {

    private lateinit var loadViewModel:LoadViewModel
    private var timer: Timer = Timer("loading")
    private var tips = mutableListOf("Подорожуй країною і отримуй за це бали!", "Чи відвідав ти таїровські котли?", "А?", "Не лізь у ЛДНР", "Чий Крим?",
            "Змагайся з друзями за звання найбільшого мандрівника")
    @SuppressLint("SetTextI18n", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        initViewModel()
        MapsInitializer.initialize(this)

        var googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if(googleSignInAccount!=null){
            var idToken = googleSignInAccount.idToken
            Executors.newSingleThreadExecutor().execute {
                loadViewModel.getAccountData(idToken)
            }
        }
        var counter = 0

        tips.shuffle()
        timer.scheduleAtFixedRate(timerTask {
            var index = counter%tips.size
            var textView = findViewById<TextView>(R.id.tips)
            runOnUiThread {
                textView.text = tips[index]
                counter++
            }
        }, 0, 5000)
        var statusBar = findViewById<TextView>(R.id.status)
        Executors.newSingleThreadExecutor().execute{
            loadViewModel.downloadRegionsBorders()
            runOnUiThread { statusBar.text = "Загрузка: 25%" }
            loadViewModel.getSights()
            runOnUiThread { statusBar.text = "Загрузка: 50%" }
            loadViewModel.getFriends()
            runOnUiThread { statusBar.text = "Загрузка: 75%" }
            loadViewModel.getVisitedSights()
            runOnUiThread {
                statusBar.text = "Загрузка: 100%"
                var intent = Intent(this, MainActivity::class.java)
                listOf(
                    Intent.FLAG_ACTIVITY_CLEAR_TASK,
                    Intent.FLAG_ACTIVITY_NEW_TASK,
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                ).forEach { x->
                    intent.addFlags(x)
                }
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        RestartManager.fragmentManager = supportFragmentManager
    }

    private fun initViewModel() {
        loadViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
        ).get(LoadViewModel::class.java)
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }


}