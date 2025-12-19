package com.usnine.scheduler.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.usnine.scheduler.BuildConfig
import com.usnine.scheduler.R
import com.usnine.scheduler.worker.ScheduleCheckWorker
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var crashlytics: FirebaseCrashlytics

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            crashlytics.isCrashlyticsCollectionEnabled = false
        }
        createNotificationChannel()
        setupDailyScheduleCheckWork()
    }

    private fun createNotificationChannel() {
        val ctx = applicationContext
        val name = ctx.getString(R.string.notification_alarm_name)
        val descriptionText = ctx.getString(R.string.notification_alarm_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(ScheduleCheckWorker.CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setupDailyScheduleCheckWork() {
        val workManager = WorkManager.getInstance(this)
        val workName = ScheduleCheckWorker.WORK_NAME

        val now = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, ScheduleCheckWorker.TARGET_HOUR)
            set(Calendar.MINUTE, ScheduleCheckWorker.TARGET_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val initialDelay = if (targetTime.before(now)) {
            0L
        } else {
            targetTime.timeInMillis - now.timeInMillis
        }

        val dailyWorkRequest = OneTimeWorkRequestBuilder<ScheduleCheckWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
