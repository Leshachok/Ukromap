package architecture.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import architecture.AppRepository

class LoadViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: AppRepository = AppRepository.getInstance(application)

    fun downloadRegionsBorders(){
        repository.downloadRegionsBorders()
    }

    fun getAccountData(idToken: String?) {
        repository.getAccountData(idToken)
    }

    fun getSights(){
        repository.getSights()
    }

    fun getFriends() {
        repository.getFriends(true)
    }

    fun getVisitedSights() {
        repository.getVisitedSights(true)
    }

}