package com.usnine.scheduler.ui.navigation

sealed class Screen(val route: String) {
    data object Calendar : Screen(CALENDAR_ROUTE)
    data object Search : Screen(SEARCH_ROUTE)
    data object Add : Screen(ADD_ROUTE) {
        fun createRoute(date: String) = "add_screen?date=$date"
    }
}

const val CALENDAR_ROUTE = "calendar_route"
const val SEARCH_ROUTE = "search_route"
const val ADD_ROUTE = "add_screen?date={date}"
