package com.example.scheduler.repository

import com.example.scheduler.data.Schedule
import com.example.scheduler.data.ScheduleDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// Hilt/Dagger를 사용하지 않는다면 @Inject와 생성자 주입은 직접 인스턴스화로 대체
@Singleton
class ScheduleRepository @Inject constructor(private val scheduleDao: ScheduleDao) {

    fun getAllSchedules(): Flow<List<Schedule>> {
        return scheduleDao.getAll()
    }

    suspend fun insertSchedule(schedule: List<Schedule>) {
        scheduleDao.insertAll(schedule)
    }

    suspend fun insertNewSchedule(newSchedule: Schedule): Long { // 반환 타입 Long으로 변경
        return scheduleDao.insertSchedule(newSchedule) // DAO의 insertSchedule 함수 호출
    }

    suspend fun deleteSchedule() {
        scheduleDao.deleteAllEvents()
    }

    suspend fun update(schedule: Schedule) {
        scheduleDao.update(schedule)
    }

    suspend fun delete(schedule: Schedule) {
        scheduleDao.delete(schedule)
    }

    // 또는 동기적으로 가져오는 함수 (주의: 메인 스레드에서 호출 금지)
    // suspend fun getAllSchedulesList(): List<Schedule> {
    //     return scheduleDao.getAllSchedulesList() // DAO에 해당 함수가 정의되어 있어야 함
    // }
}