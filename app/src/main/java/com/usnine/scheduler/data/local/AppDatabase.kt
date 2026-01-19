package com.usnine.scheduler.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.usnine.scheduler.data.model.Schedule

@Database(entities = [ScheduleEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao

}
