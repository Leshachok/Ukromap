package utilrecycle

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ukromap.R
import util.AvatarManager

class AdapterAvatars(var list: List<Avatar>, var context: Activity, var mySights: Int, var currentAvatar: Int) :
    RecyclerView.Adapter<AdapterAvatars.ViewHolder>() {

    lateinit var chosenButton:Button


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var avatarImage:ImageView = itemView.findViewById(R.id.avatarimage)
        var avatarName: TextView = itemView.findViewById(R.id.avatarname)
        var chooseButton: Button = itemView.findViewById(R.id.choosebutton)
        var textField: TextView = itemView.findViewById(R.id.text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view:View = LayoutInflater.from(parent.context).inflate(R.layout.avatar_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var photo = AvatarManager.avatars[list[position].imageId]
        var bitmap = BitmapFactory.decodeResource(context.resources, photo!!)
        holder.avatarImage.setImageBitmap(bitmap)
        holder.avatarName.text = list[position].name

        var avatarId = list[position].imageId
        var sightsAmount = list[position].count
        var currentButton = holder.chooseButton
        currentButton.text = "Обрати"
        currentButton.setTextColor(Color.BLACK)
        var text = holder.textField
        text.text = list[position].description

        if(mySights >= sightsAmount){
            if(avatarId == currentAvatar){
                chosenButton = currentButton
                currentButton.text = "Обрано"
                currentButton.setTextColor(Color.GREEN)
            }

            currentButton.setOnClickListener {
                try {
                    chosenButton.text = "Обрати"
                    chosenButton.setTextColor(Color.BLACK)
                }catch (e: UninitializedPropertyAccessException){ }

                currentAvatar = avatarId
                var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
                preferences.edit().putInt("avatar", avatarId).apply()

                currentButton.text = "Обрано"
                currentButton.setTextColor(Color.GREEN)
                chosenButton = currentButton

            }
        }else{
            holder.chooseButton.visibility = View.GONE
            var number = sightsAmount - mySights
            text.width = 230
            text.text = "Щоб отримати фото, відвідайте ще $number пам'яток"
            text.setTextColor(Color.RED)
        }

    }

}