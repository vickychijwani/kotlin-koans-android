package me.vickychijwani.kotlinkoans.data

import android.util.Base64
import com.google.gson.*
import me.vickychijwani.kotlinkoans.KotlinKoansApplication
import me.vickychijwani.kotlinkoans.analytics.Analytics
import me.vickychijwani.kotlinkoans.network.KotlinKoansApiService
import me.vickychijwani.kotlinkoans.util.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

object KoanRepository {

    const val APP_STATE_CODE = "state:code"
    const val APP_STATE_LAST_RUN_STATUS = "state:last-run-status"

    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://try.kotlinlang.org/")
            .client(KotlinKoansApplication.getInstance().getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    private val api = retrofit.create(KotlinKoansApiService::class.java)

    private var listKoansCall: Call<KoanFolders>? = null
    private var getKoanCall: Call<Koan>? = null
    private var runKoanCall: Call<KoanRunResults>? = null

    fun listKoans(callback: (KoanFolders) -> Unit, errorHandler: () -> Unit) {
        listKoansCall?.cancel()
        listKoansCall = api.listKoans()
        listKoansCall!!.enqueue(object : Callback<KoanFolders> {
            override fun onResponse(call: Call<KoanFolders>, response: Response<KoanFolders>) {
                if (response.isSuccessful && response.body() != null) {
                    callback(LocalDataStore.augment(response.body()))
                } else {
                    errorHandler()
                    logError { "Failed to fetch koan list" }
                    logApiCallFailure(response)
                }
            }

            override fun onFailure(call: Call<KoanFolders>, e: Throwable) {
                if (! call.isCanceled) {
                    errorHandler()
                    reportNonFatal(e)
                }
            }
        })
    }

    fun getKoan(id: String, callback: (Koan) -> Unit, errorHandler: () -> Unit) {
        getKoanCall?.cancel()
        getKoanCall = api.getKoan(id)
        getKoanCall!!.enqueue(object : Callback<Koan> {
            override fun onResponse(call: Call<Koan>, response: Response<Koan>) {
                if (response.isSuccessful && response.body() != null) {
                    callback(LocalDataStore.augment(response.body()))
                } else {
                    errorHandler()
                    logError { "Failed to fetch koan id = $id" }
                    logApiCallFailure(response)
                }
            }

            override fun onFailure(call: Call<Koan>, e: Throwable) {
                if (! call.isCanceled) {
                    errorHandler()
                    reportNonFatal(e)
                }
            }
        })
    }

    fun runKoan(koan: Koan, callback: (KoanRunResults) -> Unit, errorHandler: (Koan) -> Unit) {
        // TODO assuming only a single modifiable file!
        val modifiableFile = koan.getModifiableFile()
        val runInfo = KoanRunInfo(
                id = koan.id,
                name = koan.name,
                files = listOf(modifiableFile),
                readOnlyFileNames = koan.getReadOnlyFiles().map { it.name }
        )
        val runInfoJson = Gson().toJson(runInfo)
        // cancel any existing request, so we don't update the UI owing to stale responses later
        runKoanCall?.cancel()
        runKoanCall = api.runKoan(modifiableFile.name, runInfoJson)
        runKoanCall!!.enqueue(object : Callback<KoanRunResults> {
            override fun onResponse(call: Call<KoanRunResults>, response: Response<KoanRunResults>) {
                if (response.isSuccessful && response.body() != null) {
                    val runResults = response.body()
                    val runStatus = runResults.getStatus()
                    LocalDataStore.saveRunStatus(koan, runStatus)
                    Analytics.logRunStatus(koan, runStatus)
                    callback(runResults)
                } else {
                    errorHandler(koan)
                    logError { "Failed to run koan id = ${koan.id}" }
                    logApiCallFailure(response)
                }
            }

            override fun onFailure(call: Call<KoanRunResults>, e: Throwable) {
                if (! call.isCanceled) {
                    errorHandler(koan)
                    reportNonFatal(e)
                }
            }
        })
    }

    fun <T> logApiCallFailure(response: Response<T>) {
        // log the failure to Crashlytics for insight into when and why this happens
        try {
            logError { "Response code: ${response.code()}" }
            logError { "Response: ${response.errorBody()?.string()}" }
        } catch (e: Exception) {
            logError { "API call failed, but this exception was thrown when trying to log the " +
                       "HTTP response:" }
            logException(e)
        }
        reportNonFatal(ApiCallFailedException())
    }

    fun saveKoan(koan: Koan) {
        LocalDataStore.saveCode(koan)
    }

    fun deleteSavedKoan(koan: Koan) {
        LocalDataStore.deleteSavedInfo(koan)
    }

    fun augmentWithLocalData(koanFolders: KoanFolders): KoanFolders {
        return LocalDataStore.augment(koanFolders)
    }

    fun augmentWithLocalData(koan: Koan): Koan {
        return LocalDataStore.augment(koan)
    }



    // private stuff
    private object LocalDataStore {
        private const val KEY_VALUE_SEP = ": "

        fun saveCode(koan: Koan) {
            val prefs = Prefs.with(KotlinKoansApplication.getInstance())
            val codeMap = prefs
                    .getStringSet(APP_STATE_CODE, setOf())
                    .toStringPrefMap()
            codeMap[koan.getModifiableFile().id] = koan.getModifiableFile().contents
            prefs.edit().putStringSet(APP_STATE_CODE,
                    codeMap.toStringPrefSet()).apply()
        }

        fun deleteSavedInfo(koan: Koan) {
            val prefs = Prefs.with(KotlinKoansApplication.getInstance())
            // delete associated run status info
            val runStatusMap = Prefs.with(KotlinKoansApplication.getInstance())
                    .getStringSet(APP_STATE_LAST_RUN_STATUS, mutableSetOf())
                    .toRunStatusPrefMap()
            val newRunStatusMap = runStatusMap.filterKeys { it != koan.id }
            prefs.edit().putStringSet(APP_STATE_LAST_RUN_STATUS,
                    newRunStatusMap.toRunStatusPrefSet()).apply()
            // delete saved code
            val codeMap = Prefs.with(KotlinKoansApplication.getInstance())
                    .getStringSet(APP_STATE_CODE, mutableSetOf())
                    .toStringPrefMap()
            val koanFileIds = koan.files.map { it.id }
            val newCodeMap = codeMap.filterKeys { it !in koanFileIds }
            prefs.edit().putStringSet(APP_STATE_CODE,
                    newCodeMap.toStringPrefSet()).apply()
        }

        fun augment(koan: Koan): Koan {
            val codeMap = Prefs.with(KotlinKoansApplication.getInstance())
                    .getStringSet(APP_STATE_CODE, mutableSetOf())
                    .toStringPrefMap()
            val runStatusMap = Prefs.with(KotlinKoansApplication.getInstance())
                    .getStringSet(APP_STATE_LAST_RUN_STATUS, mutableSetOf())
                    .toRunStatusPrefMap()
            return koan.copy(files = koan.files.map { f ->
                return@map codeMap[f.id]?.let { f.copy(contents = it) } ?: f
            }, lastRunStatus = runStatusMap[koan.id])
        }

        fun saveRunStatus(koan: Koan, status: RunStatus) {
            val prefs = Prefs.with(KotlinKoansApplication.getInstance())
            val runStatusMap = prefs
                    .getStringSet(APP_STATE_LAST_RUN_STATUS, setOf())
                    .toRunStatusPrefMap()
            runStatusMap[koan.id] = status
            prefs.edit().putStringSet(APP_STATE_LAST_RUN_STATUS,
                    runStatusMap.toRunStatusPrefSet()).apply()
        }

        fun augment(folders: KoanFolders): KoanFolders {
            val runStatusMap = Prefs.with(KotlinKoansApplication.getInstance())
                    .getStringSet(APP_STATE_LAST_RUN_STATUS, mutableSetOf())
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
            return this.map { "${it.key.encode()}${KEY_VALUE_SEP}${it.value.encode()}"}.toSet()
        }

        private fun Map<String, RunStatus>.toRunStatusPrefSet(): Set<String> {
            return this.map { "${it.key.encode()}${KEY_VALUE_SEP}${it.value.id.toString().encode()}"}.toSet()
        }

        private fun String.encode(): String {
            return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.DEFAULT).toString()
        }

        private fun String.decode(): String {
            return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
        }
    }


    private class ApiCallFailedException
        : RuntimeException("Failed to run koan, see logs for details")

}
