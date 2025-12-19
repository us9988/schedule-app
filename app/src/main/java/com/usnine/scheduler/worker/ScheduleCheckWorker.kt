package com.usnine.scheduler.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.usnine.scheduler.R
import com.usnine.scheduler.repository.ScheduleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class ScheduleCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduleRepository: ScheduleRepository // Hilt를 통해 Repository 주입
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val today = LocalDate.now()
            val startOfToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfToday = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val schedulesToday = scheduleRepository.getSchedulesForDateRange(startOfToday, endOfToday)

            if (schedulesToday.isNotEmpty()) {
                val firstScheduleTitle = schedulesToday.first().title
                val remainingCount = schedulesToday.size - 1
                val notificationText = if (remainingCount > 0) {
                    "$firstScheduleTitle 외 ${remainingCount}개의 일정이 더 있습니다."
                } else {
                    firstScheduleTitle
                }
                sendNotification("오늘의 일정", notificationText)
            }
            scheduleNextWork()

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private fun sendNotification(title: String, content: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(NOTIFICATION_ID, notification)
        }
    }

    private fun scheduleNextWork() {
        val now = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, TARGET_HOUR)
            set(Calendar.MINUTE, TARGET_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val delay = targetTime.timeInMillis - now.timeInMillis

        val nextWorkRequest = OneTimeWorkRequestBuilder<ScheduleCheckWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            nextWorkRequest
        )

    }

    companion object {
        const val CHANNEL_ID = "SCHEDULE_REMINDER_CHANNEL"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "dailyScheduleCheckWork"
        const val TARGET_HOUR = 7
        const val TARGET_MINUTE = 0
    }
}
