package com.usnine.scheduler.repository

import com.usnine.scheduler.data.RemoteDataSource
import com.usnine.scheduler.data.Schedule
import com.usnine.scheduler.data.ScheduleDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val remoteDataSource: RemoteDataSource
) {

    fun getAllSchedules(): Flow<List<Schedule>> {
        return scheduleDao.getAll()
    }

    suspend fun insertNewSchedule(newSchedule: Schedule) {
        return scheduleDao.insertSchedule(newSchedule)
    }

    suspend fun update(schedule: Schedule) {
        scheduleDao.update(schedule)
    }

    suspend fun delete(schedule: Schedule) {
        scheduleDao.delete(schedule)
    }

    suspend fun getSchedulesForDateRange(startDate: Long, endDate: Long): List<Schedule> {
        return scheduleDao.getSchedulesByDateRange(startDate, endDate)
    }

    fun searchSchedules(query: String): Flow<List<Schedule>> {
        return scheduleDao.searchSchedules(query)
    }

    suspend fun fetchAndSaveSchedules() {
        val remoteSchedules = remoteDataSource.fetchSchedules()
        if (remoteSchedules.isNotEmpty()) {
            scheduleDao.insertRemoteSchedules(remoteSchedules)
        }
    }
}
