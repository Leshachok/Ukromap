package serverRequests

class SightRequestClass {
    var status:Int = 0
    var data:List<SightClass> = listOf()
    var debug:Array<Any> = arrayOf()

    class SightClass {
        var longitude = 0.0
        var latitude = 0.0
        var title = ""
        var snippet = ""
        var imagetitle = ""
        var guid = ""
        var radius = 0
    }
}