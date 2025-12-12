package com.example.scheduler.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Schedule>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule): Long

    @Query("SELECT * FROM schedule")
//    @Query("SELECT * FROM schedule ORDER BY eventTimestamp DESC")
    fun getAll(): Flow<List<Schedule>>

    @Query("DELETE FROM schedule")
    suspend fun deleteAllEvents()

    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)
}
