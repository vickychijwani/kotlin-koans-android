package me.vickychijwani.kotlinkoans.features.listkoans

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.vickychijwani.kotlinkoans.KoanFolders
import me.vickychijwani.kotlinkoans.KoanRepository
import me.vickychijwani.kotlinkoans.util.crashUnless

class ListKoansViewModel(): ViewModel() {

    val UNWANTED_KOANS = listOf(
            "/Kotlin%20Koans/Introduction/Java%20to%20Kotlin%20conversion"
    )

    private val liveData: MutableLiveData<KoanFolders> = MutableLiveData()

    fun getFolders(): LiveData<KoanFolders> {
        // TODO is this the right check? what if getFolders() is called multiple times before the
        // TODO first fetch completes? then the network call will happen more than once!
        if (liveData.value == null) {
            KoanRepository.listKoans { folders ->
                // for now, keep only the subfolders of the Kotlin Koans folder
                liveData.value = folders
                        .find { it.name == "Kotlin Koans" }
                        ?.subfolders
                        ?.filterUnwantedKoans()
            }
        }
        return liveData
    }

    fun update() {
        val folders = liveData.value
        folders?.let {
            liveData.value = KoanRepository.augmentWithLocalData(folders)
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
