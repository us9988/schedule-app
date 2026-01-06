package com.usnine.scheduler.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Entity(tableName = "schedules")
data class Schedule(
    @ColumnInfo("id")
    @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo("date")
    var date: Long,
    @ColumnInfo("title")
    var title: String,
    @ColumnInfo("memo")
    var memo: String,
    @ColumnInfo("isImportant")
    var isImportant: Boolean = false
)

val Schedule.localDate: LocalDate
    get() = Instant.ofEpochMilli(this.date).atZone(ZoneId.systemDefault()).toLocalDate()

val Long.localDate: LocalDate
    get() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
