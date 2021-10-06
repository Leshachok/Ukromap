package serverRequests

class VisitedSightsClass {
    var status:Int = 0
    var data:List<UserSight> = listOf()
    var debug:Array<Any> = arrayOf()
}

class UserSight{
    var user = ""
    var sight = ""
}