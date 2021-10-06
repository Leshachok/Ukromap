package mapobjects

import android.content.Context
import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

class RegionManager {

    companion object{

        lateinit var context: Context

        private fun polygonDesign(polygon: Polygon){
//            listRegion.forEach { region ->
//                if(region.regname == polygon.tag.toString()){
//                    if(region.isVisited) polygon.fillColor = Color.valueOf(0f, 250f, 0f, 0.15f).toArgb()
//                    else polygon.fillColor = Color.valueOf(50f, 0f, 0f, 0.1f).toArgb()
//                }
//            }
            polygon.fillColor = Color.valueOf(0f, 50f, 0f, 0.1f).toArgb()
            polygon.isClickable = true
            polygon.strokeWidth = 4f

        }

        fun createPolygons(googleMap: GoogleMap){

            UkraineRegion.listRegions.forEach { reg->
                if(reg.bordersList.isNotEmpty() && reg.name=="ukraine"){
                    reg.polygon = googleMap.addPolygon(
                        PolygonOptions()
                            .addAll(reg.bordersList)
                    )
                    reg.polygon!!.tag = reg.name
                }
            }


            UkraineRegion.listRegions.forEach { reg-> reg.polygon?.let { polygonDesign(it) } }
        }

        var I = Double.POSITIVE_INFINITY

        var graph = arrayOf(
            arrayOf(0, 1, I, I, 1, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, 1, I),
            arrayOf(1, 0, I, 1, 1, I, I, I, I, I, I, I, 1, I, I, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, 0, I, I, I, I, I, I, 1, I, 1, 1, 1, 1, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, 1, I, 0, I, I, 1, I, I, I, I, I, 1, I, I, I, I, 1, I, I, I, I, I, I, I),
            arrayOf(1, 1, I, I, 0, I, I, I, I, 1, I, I, 1, I, I, I, I, I, I, 1, I, I, I, 1, I),

            arrayOf(I, I, I, I, I, 0, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, 0, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, 0, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, 0, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, 0, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, 0, I, I, I, I, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, 0, I, I, I, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, 0, I, I, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, I, 0, I, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, I, I, 0, I, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, 0, I, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, 0, I, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, 0, I, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, 0, I, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, 0, I, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, 0, I, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, 0, I, I, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, 0, I, I),
            arrayOf(1, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, 0, I),
            arrayOf(I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, I, 0)
        )

    }
}