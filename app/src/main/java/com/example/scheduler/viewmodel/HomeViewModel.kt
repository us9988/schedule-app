package com.example.scheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scheduler.data.Schedule
import com.example.scheduler.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // Hilt 사용 시
class HomeViewModel @Inject constructor( // Hilt 사용 시, 아니면 직접 Repository 주입
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val TAG = "HomeViewModel"

    // UI에 데이터를 표시해야 한다면 StateFlow 등으로 노출
    val schedules: StateFlow<List<Schedule>> = scheduleRepository.getAllSchedules()
        .map { schedules ->
            schedules.sortedBy { it.date }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // 5초 동안 구독자가 없으면 Flow 중지
            initialValue = emptyList()
        )

    init {
        Log.d(TAG, "HomeViewModel initialized")
        logAllSchedulesOnStart() // ViewModel 초기화 시 로그 출력
    }

    private fun logAllSchedulesOnStart() {
        viewModelScope.launch { // 코루틴 스코프 내에서 데이터 접근
            scheduleRepository.getAllSchedules().collect { scheduleList -> // Flow를 collect
                if (scheduleList.isNotEmpty()) {
                    Log.d(TAG, "---- All Schedules in DB ----")
                    scheduleList.forEach { schedule ->
                        Log.d(TAG, schedule.toString())
                    }
                    Log.d(TAG, "-----------------------------")
                } else {
                    Log.d(TAG, "No schedules found in DB.")
                }
            }
            // 만약 DAO에 suspend fun getAllSchedulesList(): List<Schedule> 같은 함수가 있다면:
            /*
            try {
                val scheduleList = scheduleRepository.getAllSchedulesList() // Repository를 통해 호출
                if (scheduleList.isNotEmpty()) {
                    Log.d(TAG, "---- All Schedules in DB (List) ----")
                    scheduleList.forEach { schedule ->
                        Log.d(TAG, schedule.toString())
                    }
                    Log.d(TAG, "------------------------------------")
                } else {
                    Log.d(TAG, "No schedules found in DB (List).")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching schedules: ", e)
            }
            */
        }
    }

    // 필요하다면 UI에서 직접 호출할 수 있는 함수로 만들 수도 있습니다.
    fun logSchedulesManually() {
        logAllSchedulesOnStart()
    }

    fun addNewSchedule(
        title: String,
        description: String,
        date: Long,
        isImportant: Boolean = false
    ) {
        // 1. 새로운 Schedule 객체 생성
        val newSchedule = Schedule(
            // id는 autoGenerate = true 이므로 0 또는 기본값으로 두면 Room이 자동 생성
            title = title,
            description = description,
            date = date,
            isImportant = isImportant
        )

        // 2. viewModelScope를 사용하여 코루틴 내에서 Repository의 insert 함수 호출
        viewModelScope.launch(Dispatchers.Default) { // Room DB 작업은 메인 스레드에서 하면 안 됨
            try {
                scheduleRepository.insertNewSchedule(newSchedule)
                // 성공적으로 삽입된 후 필요한 작업 (예: UI 상태 업데이트, 로그 출력 등)
                // Log.d("HomeViewModel", "New schedule inserted: $newSchedule")
            } catch (e: Exception) {
                // 오류 처리 (예: 로그 출력, 사용자에게 알림 등)
                // Log.e("HomeViewModel", "Error inserting schedule", e)
            }
        }
    }
}
