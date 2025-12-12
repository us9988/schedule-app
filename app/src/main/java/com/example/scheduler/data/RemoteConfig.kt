package com.example.scheduler.data

import android.util.Log
import com.example.scheduler.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

object RemoteConfig {

    fun getDefSchedule() {
        Log.e("TAG", "fetch:Start")
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        val schedule = remoteConfig.getString("def_schedule")
    }
}