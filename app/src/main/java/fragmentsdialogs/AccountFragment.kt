package fragments

import activities.AvatarChosingActivity
import activities.LoginActivity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import architecture.viewmodels.MainViewModel
import com.example.ukromap.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import fragmentsdialogs.AppInfoDialog
import fragmentsdialogs.EditNicknameDialog
import kotlinx.android.synthetic.main.fragment_account.*
import mapobjects.SightManager
import util.AvatarManager
import java.util.*
import kotlin.concurrent.thread

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mainViewModel: MainViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private var name:String = ""
    private var visitedCount:Int = 0
    private var avatarId: Int = 0
    private var EDIT_NICKNAME_REQUEST_CODE = 100
    private var INVALID_TEXT_CODE = 102
    private var CHOOSE_AVATAR_CODE = 200

    private lateinit var accountName:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        initViewModel()

        getGoogleClient()
        getUserData()

        launchAppInfoDialog()

    }


    @SuppressLint("CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view:View = inflater.inflate(R.layout.fragment_account, container, false)

        setImageProfile(view)

        var editnicknamebtn = view.findViewById<ImageView>(R.id.editnicknamebutton)
        editnicknamebtn.setOnClickListener {
            openEditNicknameDialog()
        }

        var chooseAvatarButton:ImageView = view.findViewById(R.id.choosephotobutton)
        chooseAvatarButton.setOnClickListener {
            var intent = Intent(context, AvatarChosingActivity::class.java)
            startActivityForResult(intent, CHOOSE_AVATAR_CODE)
        }

        var appInfoPhoto = view.findViewById<ImageView>(R.id.accountappinfobutton)
        appInfoPhoto.setOnClickListener {
            var dialog = AppInfoDialog(requireActivity())
            dialog.show(requireActivity().supportFragmentManager, "app_info")
        }

        accountName = view.findViewById(R.id.avatarname)
        accountName.text = name

        var visitedCountView:TextView = view.findViewById(R.id.sightcount)
        visitedCountView.text = "Пам'ятки: $visitedCount"

        var logOutButton:ImageView = view.findViewById(R.id.log_out_button)
        logOutButton.setOnClickListener {
            signOut()
        }

        return view
    }

    private fun openEditNicknameDialog(){
        var time  = mainViewModel.getLastNicknameChangingTime()
        var currentTime = System.currentTimeMillis()

        if(currentTime - time < 86400000){
            Toast.makeText(context, "Сьогодні ім'я вже було змінено!", Toast.LENGTH_SHORT).show()
            return
        }

        var dialog = EditNicknameDialog(requireActivity())
        dialog.setTargetFragment(this, EDIT_NICKNAME_REQUEST_CODE)
        dialog.show(requireActivity().supportFragmentManager, "edit_nickname")

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode){
            EDIT_NICKNAME_REQUEST_CODE -> {
                when (resultCode){
                    EDIT_NICKNAME_REQUEST_CODE -> {
                        var name = data!!.getStringExtra("nickname") ?: "Помилка"
                        var time = data.getLongExtra("time", 0)
                        mainViewModel.saveLastNicknameChangingTime(time)
                        accountName.text = name
                        silentSignIn { _ -> mainViewModel.changeNickname(name) }
                    }
                    INVALID_TEXT_CODE -> {
                        Toast.makeText(context, "Закороткий нік!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            CHOOSE_AVATAR_CODE ->{
                when(resultCode) {
                    CHOOSE_AVATAR_CODE -> {

                        var currentavatar = mainViewModel.getAvatar()
                        if(currentavatar != avatarId){
                            avatarId = currentavatar
                            view?.let { setImageProfile(it) }
                            silentSignIn {
                                mainViewModel.changeAvatar(currentavatar)
                            }
                        }

                    }
                }
            }
        }
    }

    private fun getUserData() {
        name = mainViewModel.getProfileName()
        visitedCount = mainViewModel.getVisitedNumber()
        avatarId = mainViewModel.getAvatar()
    }

    private fun setImageProfile(view: View){
        var imageview:ImageView = view.findViewById(R.id.profileImage)
        var avatarPhoto = AvatarManager.avatars[avatarId]
        var bitmap = BitmapFactory.decodeResource(resources, avatarPhoto!!)
        imageview.setImageBitmap(bitmap)
    }

    private fun getGoogleClient() {
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)


    }

    private fun launchAppInfoDialog() {
        var preferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        if(!preferences.contains("first_launch_dialog")){
            var editor = preferences.edit()
            editor.putBoolean("first_launch_dialog", true)
            editor.apply()
            var dialog = AppInfoDialog(requireActivity())
            dialog.show(requireActivity().supportFragmentManager, "app_info")
        }
    }

    private fun signOut() {
        SightManager.listSights.clear()
        var preferences = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE)
        var editor = preferences.edit()
        editor.clear()
        editor.apply()
        preferences = requireContext().getSharedPreferences("sights", Context.MODE_PRIVATE)
        editor = preferences.edit()
        editor.remove("set")
        editor.apply()
        googleSignInClient.signOut().addOnCompleteListener{
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun initViewModel() {
        mainViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.
        getInstance(requireActivity().application)).get(MainViewModel::class.java)
    }

    private fun silentSignIn(callback: (String) -> Unit){
        var preferences = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE)
        googleSignInClient.silentSignIn()
            .addOnCompleteListener { task -> run{
                var token = task.result.idToken ?: ""
                var oldtoken = preferences.getString("idToken", "")
                if(token != oldtoken){
                    preferences.edit().putString("idToken", token).apply()
                    Log.d("token", "idtoken changed $token")
                }
                thread {
                    callback(token)
                }
            }  }
    }

}