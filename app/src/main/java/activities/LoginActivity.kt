package activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukromap.R
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import fragmentsdialogs.RestartDialog
import util.RestartManager


class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient:GoogleSignInClient
    private var SIGN_IN_INTENT_CODE = 1200
    private lateinit var mapDefaultValues:Map<EditText, String>
    private var idToken:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.silentSignIn()
            .addOnCompleteListener { task -> run{
                handleSignInResult(task)
            }  }

        setUI()


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUI() {
//        var editNickname:EditText = findViewById(R.id.textView5)
//        var editMail:EditText = findViewById(R.id.textView3)
//        var editPassword:EditText = findViewById(R.id.textView4)

//        mapDefaultValues = mapOf(
//            editNickname to "Придумайте ник",
//            editMail to "Введите свою почту",
//            editPassword to "Введите пароль"
//        )

        var button = findViewById<SignInButton>(R.id.sign_in_button)
        button.setOnClickListener { signIn() }

//        mapDefaultValues.keys.forEach { x-> kotlin.run {
//            x.setOnTouchListener { v, event ->
//                if(event.action == MotionEvent.ACTION_DOWN){
//                    if(x.editableText.toString() == mapDefaultValues[x]){
//                        x.editableText.clear()
//                    }
//                }
//                return@setOnTouchListener false
//            }
//        } }

//        var registerButton = findViewById<TextView>(R.id.textView8)
//        registerButton.setOnClickListener {
//            mapDefaultValues.keys.forEach { x-> run{
//                if(x.text.toString() == mapDefaultValues[x]){
//                    Toast.makeText(this, "а введи хоть что-то", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//            }
//            Toast.makeText(this, "а входи через гугл", Toast.LENGTH_SHORT).show()
//            }
//        }

    }

    override fun onResume() {
        super.onResume()
        RestartManager.fragmentManager = supportFragmentManager
    }

    private fun signIn() {
        if(isNetworkConnected()){
            val signInIntent: Intent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, SIGN_IN_INTENT_CODE)
        }else{
            var dialog = RestartDialog(false)
            dialog.show(supportFragmentManager, "no_internet")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SIGN_IN_INTENT_CODE){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            idToken = account.idToken
            if(isNetworkConnected()) openLoadingActivity()
            else onRestart()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=" + e.statusCode)

        }
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    @SuppressLint("CommitPrefEdits")
    fun openLoadingActivity(){
        var preferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        var editor = preferences.edit()
        editor.putString("idToken", idToken)
        editor.apply()
        var intent = Intent(this, LoadActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        startActivity(intent)
    }

}