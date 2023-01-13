package com.lcl.lclmeasurementtool.ui

import android.content.Intent
import android.util.Log
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.lcl.lclmeasurementtool.MainActivity2
import com.lcl.lclmeasurementtool.networking.NetworkMonitor
import com.lcl.lclmeasurementtool.ui.navigation.*
import com.yzq.zxinglibrary.android.CaptureActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


@Composable
fun rememberAppState(
    windowSizeClass: WindowSizeClass,
    networkMonitor: NetworkMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController()
) : AppState {
    return remember(navController, coroutineScope, windowSizeClass, networkMonitor) {
        AppState(navController, coroutineScope, windowSizeClass, networkMonitor)
    }
}

@Stable
class AppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope,
    val windowSizeClass: WindowSizeClass,
    val networkMonitor: NetworkMonitor
) {

    var shouldShowSettingsDialog by mutableStateOf(false)
        private set


    val currentTopLevelDestination : TopLevelDestination?
        @Composable get() = when(currentDestination?.route) {
            homeNavigationRoute -> TopLevelDestination.HOME
            historyRoute -> TopLevelDestination.HISTORY
            else -> null
        }

    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        Log.d("AppState","navigate to $topLevelDestination")
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