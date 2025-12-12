package com.example.scheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scheduler.data.Schedule
import com.example.scheduler.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor( // Hilt 사용 시, 아니면 직접 Repository 주입
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    // 1. 선택된 날짜를 관리할 private MutableStateFlow 추가
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    // 2. 외부에서 관찰할 수 있도록 public StateFlow 노출
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    // 1. currentMonth 상태를 ViewModel에서 관리
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // 2. 월을 변경하는 함수들 추가
    fun onPreviousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun onNextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun onMonthSelected(year: Int, month: Int) {
        _currentMonth.value = YearMonth.of(year, month)
    }
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

    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            // scheduleRepository에 update 함수가 있다고 가정
            scheduleRepository.update(schedule)
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            // scheduleRepository에 delete 함수가 있다고 가정
            scheduleRepository.delete(schedule)
        }
    }

    // 3. 날짜를 변경하는 함수 추가
    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addNewSchedule(
        title: String,
        description: String,
        date: Long?,
        isImportant: Boolean = false,
    ) {
        if (title.isBlank() || date == null) {
            // 사용자에게 알림(Toast, Snackbar 등)을 보내는 로직을 여기에 추가할 수 있습니다.
            return
        }
        // 1. 새로운 Schedule 객체 생성
        val newSchedule = Schedule(
            // id는 autoGenerate = true 이므로 0 또는 기본값으로 두면 Room이 자동 생성
            title = title,
            description = description,
            date = date,
            isImportant = isImportant
        )

        // 2. viewModelScope를 사용하여 코루틴 내에서 Repository의 insert 함수 호출
        viewModelScope.launch { // Room DB 작업은 메인 스레드에서 하면 안 됨
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