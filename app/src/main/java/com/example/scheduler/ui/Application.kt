package com.example.scheduler.ui

import android.app.Application
import com.example.scheduler.BuildConfig
import com.example.scheduler.data.AppDatabase
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MyApplication : Application() {
    private lateinit var database: AppDatabase
    private lateinit var firebaseAnalytics: FirebaseAnalytics


    override fun onCreate() {
        super.onCreate()
        firebaseAnalytics = Firebase.analytics
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}
