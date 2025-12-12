package com.example.scheduler.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.scheduler.ui.navigation.Screen
import com.example.scheduler.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch

@Composable
fun SchedulerApp(
    calendarViewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedDate by calendarViewModel.selectedDate.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    // 드로어 상태를 관리하는 변수 (열림/닫힘)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // 비동기 작업 (드로어 열기 / 닫기) 을 위한 코루틴 스코프
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        // 드로어 안에 들어갈 내용 (메뉴 리스트)
        drawerContent = {
            ModalDrawerSheet(
                // 화면 너비의 3분의 2 (약 66%) 정도로 설정합니다.
                // 3분의 1로 하려면 0.33f로 조절할 수 있습니다.
                modifier = Modifier.fillMaxWidth(0.66f)
            ) {
                AppDrawerContent(
                    onItemClick = {
                        scope.launch { drawerState.close() } // 클릭 시 드로어 닫기
                        // navController.navigate(route) // 실제 이동 로직
                    }
                )
            }
        }
    ) {
        Scaffold(
            // 2. 상단 바 설정
            topBar = {
                if (currentRoute != Screen.Add.route) {
                    MainTopAppBar(
                        onMenuClick = {
                            scope.launch { drawerState.open() }
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
                // << 화면이 들어올 때 기본 애니메이션 >>
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                // << 화면이 나갈 때 기본 애니메이션 >>
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                composable(Screen.Calendar.route) {
                    CalendarScreen(viewModel = calendarViewModel)
                }
                composable(
                    route = Screen.Add.route,
                    arguments = listOf(navArgument("date") {
                        type = NavType.StringType
                        nullable = true // 선택적 인자이므로 nullable = true
                    }),
                    // 들어올 때: 오른쪽에서 왼쪽으로 슬라이드
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
                    },
                    // 나갈 때: 왼쪽에서 오른쪽으로 슬라이드
                    exitTransition = {
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
                    },
                    popEnterTransition = { // 뒤로가기로 돌아올 때
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300))
                    },
                    popExitTransition = { // 뒤로가기로 현재 화면을 떠날 때
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
                    }
                ) { backStackEntry ->
                    // 5. AddScreen에 전달받은 날짜 인자를 넘겨줍니다.
                    // 인자가 없을 경우 오늘 날짜를 사용합니다.
                    val dateString = backStackEntry.arguments?.getString("date")
                    AddScreen(defaultDate = dateString, navController = navController)
                }
            }
        }
    }
}

//@Composable
//private fun BottomBar(navController: NavHostController) {
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentDestination = navBackStackEntry?.destination
//
//    NavigationBar(
//        containerColor = Color.Transparent
//    ) {
//        bottomDestinations.forEach { screen ->
//            val selected = currentDestination
//                ?.hierarchy
//                ?.any { it.route == screen.route } == true
//
//            NavigationBarItem(
//                selected = selected,
//                onClick = {
//                    navController.navigate(screen.route) {
//                        // 탭 이동 시 스택 관리 & 상태 복원
//                        popUpTo(navController.graph.startDestinationId) {
//                            saveState = true
//                        }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
//                },
//                icon = { Icon(screen.icon, contentDescription = stringResource(screen.label)) },
//                label = { Text(stringResource(screen.label)) },
//                alwaysShowLabel = false,
//                colors =
//                    NavigationBarItemDefaults.colors(
//                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
//                        unselectedIconColor = MaterialTheme.colorScheme.primary,
//                        selectedTextColor = MaterialTheme.colorScheme.onPrimary,
//                        unselectedTextColor = MaterialTheme.colorScheme.primary,
//                        indicatorColor = Color.Transparent
//                    )
//            )
//        }
//    }
//}

// 상단 바 컴포넌트 (재사용 가능)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    onMenuClick: () -> Unit,
    onAddClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "KNOTI", // 앱 타이틀
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "메뉴"
                )
            }
        },
        actions = {
            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "일정 추가"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black,
            actionIconContentColor = Color.Black
        )
    )
}

@Composable
fun AppDrawerContent(onItemClick: (String) -> Unit) {
    ModalDrawerSheet {
        // 상단 타이틀
        Text(
            text = "KNOTI",
            modifier = Modifier.padding(24.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        HorizontalDivider() // 구분선

        Spacer(modifier = Modifier.height(12.dp))

        // 메뉴 아이템 1: 홈
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("홈") },
            selected = false, // 현재 선택된 상태 로직 추가 가능
            onClick = { onItemClick("home") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        // 메뉴 아이템 2: 설정
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("설정") },
            selected = false,
            onClick = { onItemClick("settings") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}