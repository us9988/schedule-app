package com.example.scheduler.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scheduler.data.Schedule
import com.example.scheduler.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // Hilt 사용 시
class AddViewModel @Inject constructor( // Hilt 사용 시, 아니면 직접 Repository 주입
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val TAG = "HomeViewModel"
    // 이 ViewModel 인스턴스에만 해당되는 날짜 상태
    private val _selectedDateMillis = MutableStateFlow<Long?>(null) // 초기값은 null
    val selectedDateMillis: StateFlow<Long?> = _selectedDateMillis.asStateFlow()


    init {
        Log.d(TAG, "HomeViewModel initialized")
    }
    fun updateSelectedDate(dateMillis: Long?) {
        _selectedDateMillis.value = dateMillis
        Log.d(TAG, "Selected date updated (instance: ${this.hashCode()}): $dateMillis")
    }


}
