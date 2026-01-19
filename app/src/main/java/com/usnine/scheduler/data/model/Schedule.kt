package com.usnine.scheduler.data.model

import java.time.LocalDate

data class Schedule(
    val id: String,
    var localDate: LocalDate,
    var startDateString: String,
    var endDateString: String,
    val dateMillis: Long,
    var title: String,
    var memo: String,
)
