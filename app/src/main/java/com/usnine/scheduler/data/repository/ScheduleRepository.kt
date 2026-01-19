package com.usnine.scheduler.data.repository

import com.usnine.scheduler.data.local.ScheduleDao
import com.usnine.scheduler.data.local.ScheduleEntity
import com.usnine.scheduler.data.local.toDomain
import com.usnine.scheduler.data.model.Schedule
import com.usnine.scheduler.data.remote.ScheduleRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val remoteDataSource: ScheduleRemoteDataSource
) {

    suspend fun insertNewSchedule(entity: ScheduleEntity) {
        scheduleDao.insert(entity)
    }

    suspend fun update(schedule: Schedule) {
        val existingEntity = scheduleDao.getById(schedule.id) ?: return
        val updatedEntity = existingEntity.copy(
            title = schedule.title,
            memo = schedule.memo
        )
        scheduleDao.update(updatedEntity)
    }

    suspend fun delete(schedule: Schedule) {
        scheduleDao.deleteById(schedule.id)
    }

    suspend fun getSchedulesForDateRange(startDate: Long, endDate: Long): List<ScheduleEntity> {
        return scheduleDao.getSchedulesByDateRange(startDate, endDate)
    }

    fun searchSchedules(query: String): Flow<List<Schedule>> {
        val entityFlow = scheduleDao.searchSchedules(query)

        return entityFlow.map { entityList ->
            entityList.map { entity ->
                entity.toDomain()
            }
        }
    }

    /**
     * 원격 데이터를 Room에 저장 (Remote -> Local)
     */
    suspend fun syncRemoteSchedules() {
        val dtos = remoteDataSource.fetchSchedules()
        val entities = dtos.map { dto ->
            ScheduleEntity(
                id = dto.id,
                title = dto.title,
                startDateString = dto.startDateString,
                endDateString = dto.endDateString,
                startDateMillis = LocalDate.parse(dto.startDateString).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            )
        }
        scheduleDao.insert(entities)
    }

    fun getSchedulesFromLocal(): Flow<List<Schedule>> {
        return scheduleDao.getAll().map { entityList ->
            entityList.map { entity ->
                entity.toDomain()
            }
        }
    }

}
