package com.usnine.scheduler.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.usnine.scheduler.data.model.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(schedules: List<ScheduleEntity>)

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: String): ScheduleEntity?

    @Query("SELECT * FROM schedules ORDER BY startDateMillis ASC")
    fun getAll(): Flow<List<ScheduleEntity>>

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()

    @Query("SELECT * FROM schedules WHERE startDateMillis >= :startDate AND startDateMillis < :endDate")
    suspend fun getSchedulesByDateRange(startDate: Long, endDate: Long): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE title LIKE '%' || :query || '%' OR memo LIKE '%' || :query || '%' ORDER BY startDateMillis DESC")
    fun searchSchedules(query: String): Flow<List<ScheduleEntity>>
}
