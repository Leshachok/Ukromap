package activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ukromap.R
import util.AvatarManager
import util.RestartManager
import utilrecycle.AdapterAvatars

class AvatarChosingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avatar_chosing)

        var preferences = getSharedPreferences("sights", MODE_PRIVATE)
        var sightscount = preferences.getStringSet("set", mutableSetOf())!!.size
        preferences = getSharedPreferences("user", MODE_PRIVATE)
        var avatar = preferences.getInt("avatar", AvatarManager.DEFAULT_MAN_AVATAR)


        var recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        var list = AvatarManager.listAvatars
        var adapter = AdapterAvatars(list, this, sightscount, avatar)
        recyclerView.adapter = adapter

    }

    override fun onResume() {
        super.onResume()
        RestartManager.fragmentManager = supportFragmentManager
    }

    override fun finish() {
        setResult(200)
        super.finish()
    }
}