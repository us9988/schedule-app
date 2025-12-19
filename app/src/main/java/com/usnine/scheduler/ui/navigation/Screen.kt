package com.usnine.scheduler.ui.navigation

sealed class Screen(val route: String) {
    data object Calendar : Screen(CalendarRoute)
    data object Add : Screen(AddRoute) {
        fun createRoute(date: String) = "add_screen?date=$date"
    }
}

const val CalendarRoute = "calendar_route"
const val AddRoute = "add_screen?date={date}"
