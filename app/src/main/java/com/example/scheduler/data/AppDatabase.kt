package com.example.scheduler.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.firebase.sessions.dagger.Module
import com.google.firebase.sessions.dagger.Provides
import javax.inject.Singleton

@Module
@Database(entities = [Schedule::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao

}
