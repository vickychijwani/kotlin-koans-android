package me.vickychijwani.kotlinkoans

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.vickychijwani.kotlinkoans.analytics.Analytics
import me.vickychijwani.kotlinkoans.util.Prefs
import me.vickychijwani.kotlinkoans.util.reportNonFatal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KoanRepository {

    const val APP_STATE_CODE = "state:code"
    const val APP_STATE_LAST_RUN_STATUS = "state:last-run-status"

    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://try.kotl.in/")
            .client(KotlinKoansApplication.getInstance().getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    private val api = retrofit.create(KotlinKoansApiService::class.java)

    fun listKoans(callback: (KoanFolders) -> Unit) {
        api.listKoans().enqueue(object : Callback<KoanFolders> {
            override fun onResponse(call: Call<KoanFolders>, response: Response<KoanFolders>) {
                if (response.isSuccessful) {
                    callback(LocalDataStore.augment(response.body()))
                } else {
                    error { "Failed to fetch koan list" }
                }
            }

            override fun onFailure(call: Call<KoanFolders>, e: Throwable) {
                reportNonFatal(e)
            }
        })
    }

    fun getKoan(id: String, callback: (Koan) -> Unit) {
        api.getKoan(id).enqueue(object : Callback<Koan> {
            override fun onResponse(call: Call<Koan>, response: Response<Koan>) {
                if (response.isSuccessful) {
                    callback(LocalDataStore.augment(response.body()))
                } else {
                    error { "Failed to fetch koan id = $id" }
                }
            }

            override fun onFailure(call: Call<Koan>, e: Throwable) {
                reportNonFatal(e)
            }
        })
    }

    fun runKoan(koan: Koan, callback: (KoanRunResults) -> Unit) {
        // TODO assuming only a single modifiable file!
        val modifiableFile = koan.getModifiableFile()
        val runInfo = KoanRunInfo(
                id = koan.id,
                name = koan.name,
                files = listOf(modifiableFile),
                readOnlyFileNames = koan.getReadOnlyFiles().map { it.name }
        )
        val runInfoJson = Gson().toJson(runInfo)
        LocalDataStore.saveCode(koan, modifiableFile.contents)
        api.runKoan(modifiableFile.name, runInfoJson).enqueue(object : Callback<KoanRunResults> {
            override fun onResponse(call: Call<KoanRunResults>, response: Response<KoanRunResults>) {
                if (response.isSuccessful) {
                    val runResults = response.body()
                    val runStatus = runResults.getStatus()
                    LocalDataStore.saveRunStatus(koan, runStatus)
                    Analytics.logRunStatus(koan, runStatus)
                    callback(runResults)
                } else {
                    error { "Failed to run koan id = ${koan.id}" }
                }
            }

            override fun onFailure(call: Call<KoanRunResults>, e: Throwable) {
                reportNonFatal(e)
            }
        })
    }



    // private methods
    object LocalDataStore {
        private const val KEY_VALUE_SEP = ": "

        fun saveCode(koan: Koan, code: String) {
            val prefs = Prefs.with(KotlinKoansApplication.getInstance())
            val codeMap = prefs
                    .getStringSet(KoanRepository.APP_STATE_CODE, setOf())
                    .toStringPrefMap()
            codeMap[koan.getModifiableFile().id] = code
            prefs.edit().putStringSet(KoanRepository.APP_STATE_CODE,
                    codeMap.toStringPrefSet()).apply()
        }

        fun deleteSavedInfo(koan: Koan) {
            val prefs = Prefs.with(KotlinKoansApplication.getInstance())
            // delete associated run status info
            val runStatusMap = Prefs.with(KotlinKoansApplication.getInstance())
                    .getStringSet(KoanRepository.APP_STATE_LAST_RUN_STATUS, mutableSetOf())
                    .toRunStatusPrefMap()
            val newRunStatusMap = runStatusMap.filterKeys { it != koan.id }
            prefs.edit().putStringSet(KoanRepository.APP_STATE_LAST_RUN_STATUS,
                    newRunStatusMap.toRunStatusPrefSet()).apply()
            // delete saved code
            val codeMap = Prefs.with(KotlinKoansApplication.getInstance())
                    .getStringSet(KoanRepository.APP_STATE_CODE, mutableSetOf())
                    .toStringPrefMap()
            val koanFileIds = koan.files.map { it.id }
            val newCodeMap = codeMap.filterKeys { it !in koanFileIds }
            prefs.edit().putStringSet(KoanRepository.APP_STATE_CODE,
                    newCodeMap.toStringPrefSet()).apply()
        }

        fun augment(koan: Koan): Koan {
            val codeMap = Prefs.with(KotlinKoansApplication.getInstance())
                    .getStringSet(KoanRepository.APP_STATE_CODE, mutableSetOf())
                    .toStringPrefMap()
            val runStatusMap = Prefs.with(KotlinKoansApplication.getInstance())
                    .getStringSet(KoanRepository.APP_STATE_LAST_RUN_STATUS, mutableSetOf())
                    .toRunStatusPrefMap()
            return koan.copy(files = koan.files.map { f ->
                return@map codeMap[f.id]?.let { f.copy(contents = it) } ?: f
            }, lastRunStatus = runStatusMap[koan.id])
        }

        fun saveRunStatus(koan: Koan, status: RunStatus) {
            val prefs = Prefs.with(KotlinKoansApplication.getInstance())
            val runStatusMap = prefs
                    .getStringSet(KoanRepository.APP_STATE_LAST_RUN_STATUS, setOf())
                    .toRunStatusPrefMap()
            runStatusMap[koan.id] = status
            prefs.edit().putStringSet(KoanRepository.APP_STATE_LAST_RUN_STATUS,
                    runStatusMap.toRunStatusPrefSet()).apply()
        }

        fun augment(folders: KoanFolders): KoanFolders {
            val runStatusMap = Prefs.with(KotlinKoansApplication.getInstance())
                    .getStringSet(KoanRepository.APP_STATE_LAST_RUN_STATUS, mutableSetOf())
                    .toRunStatusPrefMap()
            return folders.map { augmentRecurse(it, folders, runStatusMap) }
        }

        private fun augmentRecurse(current: KoanFolder, folders: KoanFolders,
                                   runStatusMap: Map<String, RunStatus>): KoanFolder {
            if (current.koans.isNotEmpty()) {
                // base case
                return current.copy(koans = current.koans.map {
                    val status = runStatusMap[it.id]
                    it.copy(lastRunStatus = status, completed = status == RunStatus.OK)
                })
            } else if (current.subfolders.isNotEmpty()) {
                // recurse
                return current.copy(subfolders = current.subfolders.map {
                    augmentRecurse(it, folders, runStatusMap)
                })
            } else {
                return current
            }
        }

        private fun Set<String>.toStringPrefMap(): MutableMap<String, String> {
            return this.associate { entry ->
                val (key, value) = entry.split(KEY_VALUE_SEP)
                return@associate Pair(key.decode(), value.decode())
            }.toMutableMap()
        }

        private fun Set<String>.toRunStatusPrefMap(): MutableMap<String, RunStatus> {
            return this.associate { entry ->
                val (key, valueStr) = entry.split(KEY_VALUE_SEP)
                return@associate Pair(key.decode(), RunStatus.fromId(valueStr.decode().toInt()))
            }.toMutableMap()
        }

        private fun Map<String, String>.toStringPrefSet(): Set<String> {
            return this.map { "${it.key.encode()}$KEY_VALUE_SEP${it.value.encode()}"}.toSet()
        }

        private fun Map<String, RunStatus>.toRunStatusPrefSet(): Set<String> {
            return this.map { "${it.key.encode()}$KEY_VALUE_SEP${it.value.id.toString().encode()}"}.toSet()
        }

        private fun String.encode(): String {
            return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.DEFAULT).toString()
        }

        private fun String.decode(): String {
            return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
        }
    }

}
