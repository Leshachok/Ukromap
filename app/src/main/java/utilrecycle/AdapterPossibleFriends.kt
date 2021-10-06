package utilrecycle

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import architecture.viewmodels.FriendsViewModel
import com.example.ukromap.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import util.AvatarManager
import kotlin.concurrent.thread

class AdapterPossibleFriends(var list: MutableList<User>, var viewModel: FriendsViewModel, var context: Activity, var application: Application, var googleSignInClient: GoogleSignInClient) :
    RecyclerView.Adapter<AdapterPossibleFriends.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textNickname: TextView = itemView.findViewById(R.id.usernick)
        var textCount: TextView = itemView.findViewById(R.id.usersights)
        var textGuid: TextView = itemView.findViewById(R.id.userguid)
        var buttonRequest: Button = itemView.findViewById(R.id.requestbutton)
        var image: ImageView = itemView.findViewById(R.id.userphoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view:View = LayoutInflater.from(parent.context).inflate(R.layout.list_add_friend_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textNickname.text = list[position].username
        holder.textCount.text = list[position].count.toString()
        holder.textGuid.text = list[position].id
        var imageId = AvatarManager.avatars[list[position].avatar] ?: AvatarManager.DEFAULT_MAN_AVATAR
        holder.image.setImageBitmap(BitmapFactory.decodeResource(context.resources, imageId))
        holder.buttonRequest.setOnClickListener {
            silentSignIn {
                viewModel.sendFriendRequest(holder.textGuid.text.toString())
                list.removeAt(position)
                context.runOnUiThread{
                    this.notifyDataSetChanged()
                    if(list.isEmpty()){
                        Toast.makeText(context, "Нема запитів у друзі!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun silentSignIn(callback: (String) -> Unit){
        var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
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