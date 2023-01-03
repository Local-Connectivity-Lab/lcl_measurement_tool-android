package com.lcl.lclmeasurementtool.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.lcl.lclmeasurementtool.ui.navigation.*
import kotlinx.coroutines.CoroutineScope


@Composable
fun rememberAppState(
    windowSizeClass: WindowSizeClass,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController()
) : AppState {
    return remember(navController, coroutineScope, windowSizeClass) {
        AppState(navController, coroutineScope, windowSizeClass)
    }
}

@Stable
class AppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope,
    val windowSizeClass: WindowSizeClass
) {

    var shouldShowSettingsDialog by mutableStateOf(false)
        private set

    val currentTopLevelDestination : TopLevelDestination?
        @Composable get() = when(currentDestination?.route) {
            homeNavigationRoute -> TopLevelDestination.HOME
            historyRoute -> TopLevelDestination.HISTORY
            else -> null
        }

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        print("navigate to $topLevelDestination")
        val topLevelNavOption = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }

        when(topLevelDestination) {
            TopLevelDestination.HOME -> navController.navigateToHome(topLevelNavOption)
            TopLevelDestination.HISTORY -> navController.navigateToHistory(topLevelNavOption)
        }
    }

    fun onBackClick() {
        navController.popBackStack()
    }

    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.values().asList()

    fun setShowSettingsDialog(shouldShow: Boolean) {
        shouldShowSettingsDialog = shouldShow
    }
}