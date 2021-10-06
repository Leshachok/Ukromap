package architecture.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import architecture.AppRepository
import utilrecycle.User

class FriendsViewModel(application: Application) : AndroidViewModel(application) {
    private var repository:AppRepository = AppRepository.getInstance(application)

    fun getUsers(nickname: String):MutableList<User>{
        return repository.getUsersByNickname(nickname)
    }

    fun sendFriendRequest(id: String){
        return repository.sendFriendRequest(id)
    }

    fun getFriendRequests():MutableList<User>{
        return repository.getFriendRequests()
    }

    fun acceptFriendRequest(id: String) {
        repository.acceptFriendRequest(id)
    }

    fun denyFriendRequest(id: String) {
        repository.denyFriendRequest(id)
    }

    fun getFriends(reload: Boolean):MutableList<User> {
        return repository.getFriends(reload)
    }

    fun deleteFriend(id: String) {
        repository.deleteFriend(id)
    }
}