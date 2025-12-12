package com.example.scheduler.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.scheduler.R

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Calendar : Screen("calendar")
    data object Add : Screen("add_screen?date={date}") {
        // 특정 날짜를 포함하여 경로를 생성하는 함수
        fun createRoute(date: String) = "add_screen?date=$date"
    }
    data object Notification : Screen("noti")
    data object Setting : Screen("setting")
}
