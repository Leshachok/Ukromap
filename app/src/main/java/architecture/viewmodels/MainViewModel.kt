package architecture.viewmodels

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import architecture.AppRepository
import clojure.lang.IFn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import utilrecycle.User

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: AppRepository = AppRepository.getInstance(application)

    fun getProfileName(): String {
        return repository.getProfileName()
    }

    fun getAvatar(): Int {
        return repository.getAvatar()
    }

    fun getVisitedNumber():Int{
        return repository.getVisitedNumber()
    }

    fun getVisitedSights(): MutableSet<String>? {
        return repository.getVisitedSights(false)
    }

    fun addVisitedSight(id: String){
        repository.addVisitedSight(id)
    }

    fun changeNickname(name: String){
        repository.changeNickname(name)
    }

    fun saveLastNicknameChangingTime(long: Long){
        repository.saveLastNicknameChangingTime(long)
    }

    fun getLastNicknameChangingTime(): Long{
        return repository.getLastNicknameChangingTime()
    }

    fun changeAvatar(avatar: Int){
        repository.changeAvatar(avatar)
    }
}