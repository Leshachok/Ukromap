package serverRequests


import android.util.Log
import com.beust.klaxon.Klaxon
import java.math.BigDecimal

class RequestResult {

    companion object{

        fun getResult(json: String):List<List<BigDecimal>>{
            Log.d("json", json)
            var result = Klaxon().parse<CoordinatesRequest>(json)
            return result?.data?.coordinates ?: emptyList()
        }


    }

}