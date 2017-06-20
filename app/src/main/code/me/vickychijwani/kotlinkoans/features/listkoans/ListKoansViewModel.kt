package me.vickychijwani.kotlinkoans.features.listkoans

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.vickychijwani.kotlinkoans.data.KoanRepository
import me.vickychijwani.kotlinkoans.data.KoanFolders
import me.vickychijwani.kotlinkoans.util.crashUnless

class ListKoansViewModel(): ViewModel() {

    val UNWANTED_KOANS = listOf(
            "/Kotlin%20Koans/Introduction/Java%20to%20Kotlin%20conversion"
    )

    data class KoanFolderData(val folders: KoanFolders?, val error: Exception?)

    private val liveData: MutableLiveData<KoanFolderData> = MutableLiveData()

    fun getFolders(): LiveData<KoanFolderData> {
        // TODO is this the right check? what if getFolders() is called multiple times before the
        // TODO first fetch completes? then the network call will happen more than once!
        if (liveData.value == null) {
            KoanRepository.listKoans({ folders ->
                // for now, keep only the subfolders of the Kotlin Koans folder
                liveData.value = KoanFolderData(folders
                        .find { it.name == "Kotlin Koans" }
                        ?.subfolders
                        ?.filterUnwantedKoans(), null)
            }, {
                if (liveData.value == null) {
                    liveData.value = KoanFolderData(null, Exception())    // doesn't matter what Exception we send
                }
            })
        }
        return liveData
    }

    fun update() {
        val folders = liveData.value?.folders
        val error = liveData.value?.error
        if (folders != null) {
            liveData.value = KoanFolderData(KoanRepository.augmentWithLocalData(folders), error)
        } else {
            liveData.value = KoanFolderData(null, error)
        }
    }

    override fun onCleared() {
        // clear any subscriptions, etc.
    }


    // NOTE: folders must be exactly one level above koans, i.e., no nested folders allowed
    private fun KoanFolders.filterUnwantedKoans(): KoanFolders {
        crashUnless { this[0].koans.isNotEmpty() }
        return this.map { folder ->
            return@map folder.copy(koans = folder.koans.filter { it.id !in UNWANTED_KOANS })
        }
    }

}
