package com.lcl.lclmeasurementtool.ui

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

import com.lcl.lclmeasurementtool.networking.NetworkMonitor
import com.lcl.lclmeasurementtool.ui.navigation.TopLevelDestination
import com.lcl.lclmeasurementtool.ui.navigation.historyGraph
import com.lcl.lclmeasurementtool.ui.navigation.homeNavigationRoute
import com.lcl.lclmeasurementtool.ui.navigation.homeScreen

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class,
    ExperimentalLifecycleComposeApi::class
)
@Composable
fun LCLApp(
    windowSizeClass: WindowSizeClass,
    networkMonitor: NetworkMonitor,
//    shouldLogin: Boolean,
    appState: AppState = rememberAppState(windowSizeClass = windowSizeClass, networkMonitor = networkMonitor)
) {
//    Log.d("LCLAPP", "isLoggedIn=$shouldLogin")
//    if (shouldLogin) {
//
//        return
//    }


    if (appState.shouldShowSettingsDialog) {
        SettingsDialog(
            onDismiss = { appState.setShowSettingsDialog(false) }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val isOffline by appState.isOffline.collectAsStateWithLifecycle()
    Log.d("LCLApplication", "isOffline is $isOffline")
    LaunchedEffect(isOffline) {
        if (isOffline) {
            Log.d("LCLApplication", "show snack bar")
            snackbarHostState.showSnackbar(message = "Please connect to a cellular network before running the test", duration = SnackbarDuration.Indefinite)
        }
    }


    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0,0,0,0),
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            LCLBottomBar(destinations = appState.topLevelDestinations, onNavigateToDestination = appState::navigateToTopLevelDestination, currentDestination = appState.currentDestination)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Row(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .consumedWindowInsets(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal
                )
            )) {
            Column(modifier = Modifier.fillMaxSize()) {
                val destination = appState.currentTopLevelDestination
                if (destination != null) {
                    AppTopBar(
                        titleRes = destination.titleTextId,
                        actionIcon = LCLIcons.Settings,
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        onActionClick = { appState.setShowSettingsDialog(true) }
                    )
                }
                LCLNavHost(navController = appState.navController, onBackClick = appState::onBackClick, isOffline = isOffline)
            }
        }

    }
}

@Composable
fun LCLBottomBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier
) {

    NavigationBar(
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 0.dp) {

        destinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)

            NavigationBarItem(
                selected = selected,
                onClick = {onNavigateToDestination(destination)},
                icon = {
                    val icon = if(selected) {
                        destination.selectedIcon
                    } else {
                        destination.unselectedIcon
                    }

                    Icon(
                        imageVector = (icon as Icon.ImageVectorIcon).imageVector,
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(id = destination.iconTextId)) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(@StringRes titleRes: Int,
              actionIcon: ImageVector,
              modifier: Modifier = Modifier,
              colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
              onActionClick: () -> Unit = {}
) {
    print("hey!!!")
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = titleRes)) },
        colors = colors,
        modifier = modifier,
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
fun LCLNavHost(
    navController: NavHostController,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = homeNavigationRoute,
    isOffline: Boolean
) {
    NavHost(navController = navController, modifier = modifier, startDestination = startDestination) {
        homeScreen(isOffline)
        historyGraph()
    }
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false