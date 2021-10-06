package utilrecycle

import android.app.Activity
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import architecture.viewmodels.FriendsViewModel
import com.example.ukromap.R
import util.AvatarManager
import java.util.concurrent.Executors

class AdapterFriends(var list: MutableList<User>, var context: Activity, var viewModel: FriendsViewModel) :
    RecyclerView.Adapter<AdapterFriends.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var textNickname: TextView = itemView.findViewById(R.id.usernick)
        var textCount: TextView = itemView.findViewById(R.id.usersights)
        var deleteButton:ImageView = itemView.findViewById(R.id.deletebutton)
        var avatarPhoto:ImageView = itemView.findViewById(R.id.userphoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view:View = LayoutInflater.from(parent.context).inflate(R.layout.list_friends_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textNickname.text = list[position].username
        holder.textCount.text = list[position].count.toString()
        holder.deleteButton.setOnClickListener {
            Toast.makeText(context, "Зажміть кнопку для видалення друга!", Toast.LENGTH_SHORT).show()
        }

        holder.deleteButton.setOnLongClickListener {
            var id = list[position].id
            Executors.newSingleThreadExecutor().execute {
                viewModel.deleteFriend(id)
                context.runOnUiThread {
                    list.removeAt(position)
                    notifyDataSetChanged()
                }
            }
            false
        }
        var photo = AvatarManager.avatars[list[position].avatar]
        var bitmap = BitmapFactory.decodeResource(context.resources, photo!!)
        holder.avatarPhoto.setImageBitmap(bitmap)
    }

}