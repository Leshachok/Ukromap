package activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import architecture.viewmodels.FriendsViewModel
import com.example.ukromap.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import util.RestartManager
import utilrecycle.AdapterRequests
import utilrecycle.User
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class FriendRequestsActivity : AppCompatActivity() {
    private lateinit var viewModel:FriendsViewModel
    private var requests = mutableListOf<User>()
    private lateinit var googleSignInClient:GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        getGoogleClient()
        setContentView(R.layout.activity_friend_requests)
        var recyclerView: RecyclerView = findViewById(R.id.recycle)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        silentSignIn { _ ->
                requests = viewModel.getFriendRequests()
                runOnUiThread {
                    if(requests.isEmpty()) {
                        Toast.makeText(this, "Нема запитів у друзі!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    recyclerView.adapter = AdapterRequests(requests, viewModel, this, application, googleSignInClient)
                }

        }

    }

    override fun onResume() {
        super.onResume()
        RestartManager.fragmentManager = supportFragmentManager
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.
        getInstance(this.application)).get(FriendsViewModel::class.java)
    }

    private fun getGoogleClient() {
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun silentSignIn(callback: (String) -> Unit){
        var preferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        googleSignInClient.silentSignIn().addOnCompleteListener { task ->
            run {
                var token = task.result.idToken ?: ""
                var oldtoken = preferences.getString("idToken", "")
                if(token != oldtoken){
                    preferences.edit().putString("idToken", token).apply()
                    Log.d("token", "idtoken changed $token")
                }
                thread {
                    callback(token)
                }
            }
        }
    }

}