package serverRequests

import java.math.BigDecimal

class CoordinatesRequest {
    var status:Int = 0
    var data:RequestData = RequestData()
    var debug:Array<Any> = arrayOf()
}

class RequestData {
    var id:Int = 0
    var regname:String =""
    var coordinates:List<List<BigDecimal>> = listOf()
}