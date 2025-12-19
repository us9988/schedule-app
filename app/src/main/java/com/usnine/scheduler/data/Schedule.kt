package com.usnine.scheduler.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Entity(tableName = "schedule")
data class Schedule(
    @PrimaryKey(autoGenerate = false) val id: String,
    var date: Long,
    var title: String,
    var description: String,
    var isImportant: Boolean = false
)

val Schedule.localDate: LocalDate
    get() = Instant.ofEpochMilli(this.date).atZone(ZoneId.systemDefault()).toLocalDate()
