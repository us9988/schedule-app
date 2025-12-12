package com.example.scheduler.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class) // 앱 스코프에 설치
@Module
object DatabaseModule {

    @Provides
    @Singleton // 앱 전체에서 하나의 데이터베이스 인스턴스만 사용
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "schedule_database" // 데이터베이스 파일 이름
        )
            // .addCallback(AppDatabaseCallback()) // 필요시 콜백 추가 (예: DB 생성 시 초기 데이터 삽입)
            .build()
    }

    @Provides
    // ScheduleDao는 일반적으로 싱글톤일 필요는 없으나,
    // AppDatabase가 싱글톤이므로 여기서 제공해도 무방합니다.
    fun provideScheduleDao(appDatabase: AppDatabase): ScheduleDao {
        return appDatabase.scheduleDao()
    }
}