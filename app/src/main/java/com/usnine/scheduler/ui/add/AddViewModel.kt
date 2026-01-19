package com.usnine.scheduler.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usnine.scheduler.data.local.ScheduleEntity
import com.usnine.scheduler.data.repository.ScheduleRepository
import com.usnine.scheduler.ui.convertMillisToYmd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {
    fun addNewSchedule(
        title: String,
        memo: String,
        date: Long?,
    ): Boolean {
        if (title.isBlank() || date == null) {
            return false
        }
        val startDateString = convertMillisToYmd(date)
        val id = "$startDateString$date"
        val entity = ScheduleEntity(
            id = id,
            title = title,
            startDateString = startDateString,
            endDateString = "",
            startDateMillis = date,
            memo = memo,
        )
        viewModelScope.launch { repository.insertNewSchedule(entity) }
        return true
    }
}
