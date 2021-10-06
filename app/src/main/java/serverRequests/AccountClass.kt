package serverRequests

class AccountClass {
    var id = ""
    var newaccount = true
    var email = ""
    var username = ""
    var password = ""
    var visited = 0
    var avatar = 0
    var last_nickname_change:Long = 0
}

class AccountRequestClass{

    var status:Int = 0
    var data:Data = Data()
    var debug:Array<Any> = arrayOf()

    class Data {
        var account = AccountClass()
    }
}

