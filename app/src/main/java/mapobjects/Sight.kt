package mapobjects

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.maps.android.clustering.ClusterManager

class Sight(var marker: SightMarker, var radius:Int, var guid: String) {
    lateinit var circle: Circle
    var name = marker.getTitle()

    init {
        SightManager.listSights.add(this)
    }

    fun render(googleMap: GoogleMap, clusterManager: ClusterManager<SightMarker>){
        circle = googleMap.addCircle(CircleOptions()
            .center(marker.position)
            .radius(radius.toDouble())
            .fillColor(Color.argb(0.2f, 0f, 127f, 0f))
            .strokeWidth(1f)
        )
        clusterManager.addItem(marker)
    }
}