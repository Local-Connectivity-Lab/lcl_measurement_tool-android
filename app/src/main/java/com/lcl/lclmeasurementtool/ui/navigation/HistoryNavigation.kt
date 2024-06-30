package com.lcl.lclmeasurementtool.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lcl.lclmeasurementtool.ui.HistoryRoute

private const val historyGraphRoutePattern = "history_graph"
const val historyRoute = "history_route"

fun NavController.navigateToHistory(navOptions: NavOptions? = null) {
    this.navigate(historyGraphRoutePattern, navOptions)
}

fun NavGraphBuilder.historyGraph() {
    navigation(
        route = historyGraphRoutePattern,
        startDestination = historyRoute
    ) {
        composable(route = historyRoute) {
            HistoryRoute()
        }
    }
}