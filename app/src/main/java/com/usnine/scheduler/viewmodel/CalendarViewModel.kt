package com.usnine.scheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usnine.scheduler.data.Schedule
import com.usnine.scheduler.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor( // Hilt 사용 시, 아니면 직접 Repository 주입
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onPreviousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun onNextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun onMonthSelected(year: Int, month: Int) {
        _currentMonth.value = YearMonth.of(year, month)
    }

    val schedules: StateFlow<List<Schedule>> = scheduleRepository.getAllSchedules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleRepository.update(schedule)
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleRepository.delete(schedule)
        }
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addNewSchedule(
        title: String,
        memo: String,
        date: Long?,
        isImportant: Boolean = false,
    ): Boolean {
        if (title.isBlank() || date == null) {
            return false
        }
        val id = "$date$title"
        val newSchedule = Schedule(
            id = id,
            title = title,
            memo = memo,
            date = date,
            isImportant = isImportant
        )
        viewModelScope.launch {
            try {
                scheduleRepository.insertNewSchedule(newSchedule)
            } catch (_: Exception) {

            }
        }
        return true
    }

    fun loadSchedulesFromRemote() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                scheduleRepository.fetchAndSaveSchedules()
                // 성공 시 UI에 알림 (예: Snackbar)
            } catch (e: Exception) {
                // 실패 시
            } finally {
                _isLoading.value = false
            }
        }
    }
}