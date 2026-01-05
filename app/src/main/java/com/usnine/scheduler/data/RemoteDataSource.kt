package com.usnine.scheduler.data

import com.usnine.scheduler.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val gson: Gson
) {

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    /**
     * Firebase 원격 설정에서 일정 정보를 가져와 파싱합니다.
     */
    suspend fun fetchSchedules(): List<Schedule> {
        return try {
            remoteConfig.fetchAndActivate().await()
            val json = remoteConfig.getString(KEY_SCHEDULES)
            if (json.isBlank()) {
                return emptyList()
            }
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val rawSchedules: List<Map<String, String>> = gson.fromJson(json, type)

            rawSchedules.map { rawSchedule ->
                val dateString = rawSchedule["date"]
                val title = rawSchedule["title"]
                if (dateString.isNullOrBlank() || title.isNullOrBlank()) {
                    return emptyList()
                }

                val dateAsLong = if (dateString.isNotEmpty()) {
                    LocalDate.parse(dateString).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
                } else {
                    0L
                }
                val id = "$dateAsLong$title"
                Schedule(id, date = dateAsLong, title = title, memo = "")
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        const val KEY_SCHEDULES = "schedules"
    }

}
