package mapobjects

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.ukromap.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindow(private var layoutInflater: LayoutInflater, private var context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }

    @SuppressLint("InflateParams")
    override fun getInfoContents(marker: Marker): View? {
        var view = layoutInflater.inflate(R.layout.info_window_contents, null)
        var title = view.findViewById<TextView>(R.id.markertitle)
        title.text = marker.title
        var snippet = view.findViewById<TextView>(R.id.markersnippet)
        snippet.text = marker.snippet
        var visited = view.findViewById<TextView>(R.id.visited)

        var list = SightManager.listSights
        var position = marker.position
        var sight = list.find { sight -> sight.marker.position == position }!!
        var guid = sight.guid

        var preferences = context.getSharedPreferences("sights", Context.MODE_PRIVATE)
        var set = preferences.getStringSet("set", mutableSetOf())
        
        visited.text = if(set!!.contains(guid)) "Відвідано" else "Не відвідано"
        return view
    }
}