package fragments

import activities.AddFriendActivity
import activities.FriendRequestsActivity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import architecture.viewmodels.FriendsViewModel
import architecture.viewmodels.MainViewModel
import com.example.ukromap.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.fragment_friends.*
import utilrecycle.AdapterFriends
import utilrecycle.User
import java.lang.Exception
import java.util.concurrent.Executors
import kotlin.concurrent.thread

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

// TODO некрасивый фрагмент

class FriendsFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel: FriendsViewModel
    private lateinit var adapterFriends: AdapterFriends
    private lateinit var googleSignInClient:GoogleSignInClient
    private lateinit var list:MutableList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        initViewModel()
        getGoogleClient()
        list = viewModel.getFriends(false).toMutableList()
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        var view: View = inflater.inflate(R.layout.fragment_friends, container, false)

        var recyclerView: RecyclerView = view.findViewById(R.id.listfriends)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapterFriends = AdapterFriends(
            list, requireActivity(), viewModel)
        recyclerView.adapter = adapterFriends

        var addFriendButton = view.findViewById<Button>(R.id.add_friend_button)
        addFriendButton.setOnClickListener {
            var intent = Intent(context, AddFriendActivity::class.java)
            startActivity(intent)
        }

        var friendRequestsButton = view.findViewById<Button>(R.id.friend_requests_button)
        friendRequestsButton.setOnClickListener {
            var intent = Intent(context, FriendRequestsActivity::class.java)
            startActivity(intent)
        }

        var reload = view.findViewById<ImageView>(R.id.reload)
        reload.setOnClickListener {
            silentSignIn { _ ->
                reloadFriends()
            }
        }

        return view
    }

//    override fun onStart(){
//        super.onStart()
//        try {
//            reloadFriends()
//        }catch (e:Exception){}
//    }

    private fun reloadFriends(){
        adapterFriends.list = viewModel.getFriends(true)
        if(isAdded) {
            requireActivity().runOnUiThread {
                adapterFriends.notifyDataSetChanged()
            } }

    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.
        getInstance(requireActivity().application)).get(FriendsViewModel::class.java)
    }

    private fun getGoogleClient() {
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
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
            }
            }
    }
}