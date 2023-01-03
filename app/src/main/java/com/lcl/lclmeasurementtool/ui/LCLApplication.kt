package com.lcl.lclmeasurementtool.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.lcl.lclmeasurementtool.ui.navigation.TopLevelDestination
import com.lcl.lclmeasurementtool.ui.navigation.historyGraph
import com.lcl.lclmeasurementtool.ui.navigation.homeNavigationRoute
import com.lcl.lclmeasurementtool.ui.navigation.homeScreen

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LCLApp(
    windowSizeClass: WindowSizeClass,
    appState: AppState = rememberAppState(windowSizeClass = windowSizeClass)
) {

    if (appState.shouldShowSettingsDialog) {
        SettingsDialog(
            onDismiss = { appState.setShowSettingsDialog(false) }
        )
    }


    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0,0,0,0),
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            LCLBottomBar(destinations = appState.topLevelDestinations, onNavigateToDestination = appState::navigateToTopLevelDestination, currentDestination = appState.currentDestination)
        }
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
                LCLNavHost(navController = appState.navController, onBackClick = appState::onBackClick)
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
    startDestination: String = homeNavigationRoute
) {
    NavHost(navController = navController, modifier = modifier, startDestination = startDestination) {
        homeScreen()
        historyGraph()
    }
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false