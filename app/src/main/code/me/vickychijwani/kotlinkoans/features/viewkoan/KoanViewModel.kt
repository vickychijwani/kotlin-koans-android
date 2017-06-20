package me.vickychijwani.kotlinkoans.features.viewkoan

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.vickychijwani.kotlinkoans.data.Koan
import me.vickychijwani.kotlinkoans.data.KoanRepository

class KoanViewModel : ViewModel() {

    data class KoanData(val koan: Koan?, val error: Exception?)

    val liveData: MutableLiveData<KoanData> = MutableLiveData()

    fun loadKoan(id: String) {
        // TODO is this the right check? what if getFolders() is called multiple times before the
        // TODO first fetch completes? then the network call will happen more than once!
        val value = liveData.value
        if (value == null || value.koan?.id != id) {
            KoanRepository.getKoan(id, { liveData.value = KoanData(it, null) },
                    { liveData.value = KoanData(null, Exception()) })   // doesn't matter what exception we send
        }
    }

    fun update(deleteSavedData: Boolean = false) {
        val value = liveData.value
        value?.koan?.let { koan ->
            if (deleteSavedData) {
                KoanRepository.deleteSavedKoan(koan)
                KoanRepository.getKoan(koan.id, { liveData.value = value.copy(koan = it) },
                        { liveData.value = KoanData(null, Exception()) })   // doesn't matter what exception we send
            } else {
                liveData.value = value.copy(koan = KoanRepository.augmentWithLocalData(koan))
            }
        }
    }

    override fun onCleared() {
        // clear any subscriptions, etc.
    }

}
