package com.usnine.scheduler.di

import android.content.Context
import androidx.room.Room
import com.usnine.scheduler.data.local.AppDatabase
import com.usnine.scheduler.data.local.ScheduleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "scheduler_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideScheduleDao(appDatabase: AppDatabase): ScheduleDao {
        return appDatabase.scheduleDao()
    }
}