package mapobjects

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem


class SightMarker(
    private var latitude: Double,
    private var longitude: Double,
    private var title: String,
    private var snippet: String,
    icon: BitmapDescriptor
) : ClusterItem {

    private lateinit var markerOptions: MarkerOptions

    init {
        setMarker(
            MarkerOptions()
                .position(LatLng(latitude, longitude))
                .title(title)
                .snippet(snippet)
                .icon(icon)
        )
    }

    override fun getPosition(): LatLng {
        return LatLng(latitude, longitude)
    }

    fun getTitle(): String {
        return title
    }

    fun getSnippet(): String {
        return snippet
    }

    fun getMarker(): MarkerOptions {
        return markerOptions
    }

    fun setMarker(marker: MarkerOptions) {
        markerOptions = marker
    }

}