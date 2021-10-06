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
import androidx.recyclerview.widget.RecyclerView
import architecture.viewmodels.FriendsViewModel
import com.example.ukromap.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import util.AvatarManager
import kotlin.concurrent.thread

class AdapterRequests(var list: MutableList<User>, var viewModel: FriendsViewModel, var context: Activity, var application: Application, var googleSignInClient: GoogleSignInClient) :
    RecyclerView.Adapter<AdapterRequests.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textNickname: TextView = itemView.findViewById(R.id.usernick)
        var textGuid: TextView = itemView.findViewById(R.id.userguid)
        var buttonAccept: Button = itemView.findViewById(R.id.acceptbutton)
        var buttonDeny: Button = itemView.findViewById(R.id.denybutton)
        var image: ImageView = itemView.findViewById(R.id.userphoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view:View = LayoutInflater.from(parent.context).inflate(R.layout.list_friend_request_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textNickname.text = list[position].username
        var id = list[position].id
        holder.textGuid.text = id
        var imageId = AvatarManager.avatars[list[position].avatar] ?: AvatarManager.DEFAULT_MAN_AVATAR
        holder.image.setImageBitmap(BitmapFactory.decodeResource(context.resources, imageId))
        holder.buttonAccept.setOnClickListener {
            silentSignIn {
                viewModel.acceptFriendRequest(id)
                context.runOnUiThread{
                    list.removeAt(position)
                    this.notifyDataSetChanged()
                }
            }
        }
        holder.buttonDeny.setOnClickListener {
            silentSignIn {
                viewModel.denyFriendRequest(id)
                context.runOnUiThread{
                    list.removeAt(position)
                    this.notifyDataSetChanged()
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