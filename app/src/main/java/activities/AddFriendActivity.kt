package activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
import utilrecycle.AdapterPossibleFriends
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class AddFriendActivity : AppCompatActivity() {
    private lateinit var friendsViewModel: FriendsViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)
        initViewModel()

        getGoogleClient()

        var recyclerView: RecyclerView = findViewById(R.id.userlist)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        var adapter = AdapterPossibleFriends(mutableListOf(), friendsViewModel, this, application, googleSignInClient)
        recyclerView.adapter = adapter
        var editText = findViewById<EditText>(R.id.EditTextNickname)

        var button = findViewById<Button>(R.id.searchUserButton)
        button.setOnClickListener {
            var nickname = editText.text.toString()
            silentSignIn {
                var list = friendsViewModel.getUsers(nickname)
                runOnUiThread{
                    if(list.isEmpty()) Toast.makeText(this, "Такого користувача не існує або він вже ваш друг!", Toast.LENGTH_SHORT).show()
                    else{
                        adapter.list = list
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        RestartManager.fragmentManager = supportFragmentManager
    }

    private fun initViewModel() {
        friendsViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
        ).get(FriendsViewModel::class.java)
    }

    private fun getGoogleClient() {
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun silentSignIn(callback: (String) -> Unit){
        var preferences = this.getSharedPreferences("user", Context.MODE_PRIVATE)
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

