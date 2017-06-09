package me.vickychijwani.kotlinkoans.features.viewkoan

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.vickychijwani.kotlinkoans.Koan
import me.vickychijwani.kotlinkoans.KoanRepository

class KoanViewModel : ViewModel() {

    val liveData: MutableLiveData<Koan> = MutableLiveData()

    fun loadKoan(id: String) {
        // TODO is this the right check? what if getFolders() is called multiple times before the
        // TODO first fetch completes? then the network call will happen more than once!
        val value = liveData.value
        if (value == null || value.id != id) {
            KoanRepository.getKoan(id) { liveData.value = it }
        }
    }

    override fun onCleared() {
        // clear any subscriptions, etc.
    }

}
