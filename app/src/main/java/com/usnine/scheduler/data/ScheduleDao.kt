package com.usnine.scheduler.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRemoteSchedules(events: List<Schedule>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule)

    @Query("SELECT * FROM schedule ORDER BY date ASC")
    fun getAll(): Flow<List<Schedule>>

    @Query("DELETE FROM schedule")
    suspend fun deleteAll()

    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)

    @Query("SELECT * FROM schedule WHERE date >= :startDate AND date < :endDate")
    suspend fun getSchedulesByDateRange(startDate: Long, endDate: Long): List<Schedule>

}
