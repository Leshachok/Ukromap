package mapobjects

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class MarkerRenderer(
    context: Context,
    googleMap: GoogleMap,
    clusterManager: ClusterManager<SightMarker>
) : DefaultClusterRenderer<SightMarker>(context, googleMap, clusterManager) {

    override fun onBeforeClusterItemRendered(item: SightMarker?, markerOptions: MarkerOptions?) {
        markerOptions!!.icon(item!!.getMarker().icon)
        markerOptions.title(item.getTitle())
        markerOptions.snippet(item.getSnippet())

        super.onBeforeClusterItemRendered(item, markerOptions)
    }
}