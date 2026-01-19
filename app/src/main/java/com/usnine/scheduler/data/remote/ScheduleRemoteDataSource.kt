package com.usnine.scheduler.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun fetchSchedules(): List<ScheduleDto> = try {
        val snapshot: QuerySnapshot = firestore.collection("schedules")
            .get()
            .await()
        snapshot.documents.mapNotNull { document ->
            val rawTitle = document.getString("title") ?: ""
            val title = rawTitle.replace('·', '/')
            val startDate = document.getString("start_date") ?: ""
            val endDate = document.getString("end_date") ?: ""
            ScheduleDto(
                id = document.id,
                title = title,
                startDateString = startDate,
                endDateString = endDate
            )
        }
    } catch (_: Exception) {
        emptyList()
    }

}
