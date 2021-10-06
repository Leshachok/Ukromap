package mapobjects

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.ukromap.R
import com.fasterxml.jackson.core.SerializableString
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.clustering.ClusterManager

class SightManager {

    companion object{

        var listSights:MutableList<Sight> = mutableListOf()

        fun renderSights(googleMap: GoogleMap, clusterManager: ClusterManager<SightMarker>){
            listSights.forEach { sight-> sight.render(googleMap, clusterManager) }
        }

        fun setVisibility(boolean: Boolean){
            listSights.forEach { sight->
                run {
                    sight.marker.getMarker().visible(boolean)
                }
            }
        }

    }
}