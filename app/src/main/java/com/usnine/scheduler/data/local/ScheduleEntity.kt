package com.usnine.scheduler.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.usnine.scheduler.data.model.Schedule
import java.time.Instant
import java.time.ZoneId

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey
    val id: String,
    var title: String,
    var startDateString: String,
    var endDateString: String,
    var startDateMillis: Long,
    var memo: String = ""
)

fun ScheduleEntity.toDomain(): Schedule {
    return Schedule(
        id = this.id,
        title = this.title,
        localDate = Instant.ofEpochMilli(startDateMillis).atZone(ZoneId.systemDefault()).toLocalDate(),
        startDateString = this.startDateString,
        endDateString = this.endDateString,
        dateMillis = this.startDateMillis,
        memo = this.memo
    )
}