package me.vickychijwani.kotlinkoans.features.listkoans

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.vickychijwani.kotlinkoans.KoanFolders
import me.vickychijwani.kotlinkoans.KoanRepository

class ListKoansViewModel(): ViewModel() {

    private val liveData: MutableLiveData<KoanFolders> = MutableLiveData()

    fun getFolders(): LiveData<KoanFolders> {
        // TODO is this the right check? what if getFolders() is called multiple times before the
        // TODO first fetch completes? then the network call will happen more than once!
        if (liveData.value == null) {
            KoanRepository.listKoans { folders ->
                // for now, keep only the subfolders of the Kotlin Koans folder
                liveData.value = folders.find { it.name == "Kotlin Koans" }?.subfolders
            }
        }
        return liveData
    }

    fun update() {
        val folders = liveData.value
        folders?.let {
            liveData.value = KoanRepository.LocalData.augment(folders)
        }
    }

    override fun onCleared() {
        // clear any subscriptions, etc.
    }

}
