package com.usnine.scheduler.data.remote

import com.google.firebase.firestore.PropertyName

data class ScheduleDto(
    val id: String = "",
    val title: String = "",
    @get:PropertyName("start_date")
    val startDateString: String = "",
    @get:PropertyName("end_date")
    val endDateString: String = "",
)
