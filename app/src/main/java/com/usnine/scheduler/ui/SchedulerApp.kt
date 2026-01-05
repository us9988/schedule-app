package com.usnine.scheduler.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.usnine.scheduler.R
import com.usnine.scheduler.ui.navigation.Screen
import com.usnine.scheduler.util.HorizontalDivider
import com.usnine.scheduler.util.NavigationDrawerItem
import com.usnine.scheduler.util.Text
import com.usnine.scheduler.viewmodel.CalendarViewModel
import com.usnine.scheduler.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

@Composable
fun SchedulerApp(
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    searchViewmodel: SearchViewModel = hiltViewModel()
) {
    val selectedDate by calendarViewModel.selectedDate.collectAsStateWithLifecycle()
    val isLoading by calendarViewModel.isLoading.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen && !isLoading,
        drawerContent = {
            ModalDrawerSheet {
                AppDrawerContent(
                    onItemClick = { route ->
                        scope.launch { drawerState.close() }
                        navController.navigate(route)
                    },
                    onLoadClick = {
                        scope.launch { drawerState.close() }
                        calendarViewModel.loadSchedulesFromRemote()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute == Screen.Search.route) {
                    SearchTopAppBar(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                } else if (currentRoute == Screen.Calendar.route) {
                    MainTopAppBar(
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        onSearchClick = {
                            scope.launch { navController.navigate(Screen.Search.route) }
                        },
                        onAddClick = {
                            scope.launch {
                                val route = Screen.Add.createRoute(selectedDate.toString())
                                navController.navigate(route)
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Calendar.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                composable(Screen.Calendar.route) {
                    CalendarScreen(viewModel = calendarViewModel)
                }
                composable(Screen.Search.route) {
                    SearchScreen(
                        viewModel = searchViewmodel,
                        onItemClick = { dateTimestamp ->
                            calendarViewModel.updateSelectedDate(dateTimestamp)
                            navController.popBackStack(Screen.Calendar.route, inclusive = false)
                        }
                    )
                }
                composable(
                    route = Screen.Add.route,
                    arguments = listOf(navArgument("date") {
                        type = NavType.StringType
                        nullable = true
                    }),
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
                    },
                    exitTransition = {
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
                    },
                    popEnterTransition = {
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300))
                    },
                    popExitTransition = {
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
                    }
                ) { backStackEntry ->
                    val dateString = backStackEntry.arguments?.getString("date")
                    AddScreen(defaultDate = dateString, navController = navController)
                }
            }
        }
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}


@Composable
fun AppDrawerContent(
    onItemClick: (String) -> Unit,
    onLoadClick: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
            },
            title = {
                Text(R.string.dialog_title_load_schedules, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(R.string.dialog_message_load_schedules, style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onLoadClick()
                    }
                ) {
                    Text(R.string.dialog_ok, style = MaterialTheme.typography.bodySmall)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                    }
                ) {
                    Text(R.string.dialog_cancel, style = MaterialTheme.typography.bodySmall)
                }
            }
        )
    }
    Text(
        stringResId = R.string.app_title,
        modifier = Modifier.padding(24.dp),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    HorizontalDivider()

    Spacer(modifier = Modifier.height(12.dp))

    NavigationDrawerItem(
        iconRes = Icons.Default.Home,
        contentDescriptionRes = R.string.content_desc_drawer_home,
        labelRes = R.string.drawer_menu_home,
        onClick = {
            onItemClick(Screen.Calendar.route)
        }
    )
    NavigationDrawerItem(
        iconRes = Icons.Default.CloudDownload,
        contentDescriptionRes = R.string.content_desc_drawer_load,
        labelRes = R.string.drawer_menu_load,
        onClick = {
            showConfirmDialog = true
        }
    )
}