package com.lcl.lclmeasurementtool.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation

private const val interestsGraphRoutePattern = "history_graph"
const val historyRoute = "history_route"

fun NavController.navigateToHistoryGraph(navOptions: NavOptions? = null) {
    this.navigate(interestsGraphRoutePattern, navOptions)
}

fun NavGraphBuilder.historyGraph(
    navigateToTopic: (String) -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = interestsGraphRoutePattern,
        startDestination = historyRoute
    ) {
        composable(route = historyRoute) {
//            InterestsRoute(
//                navigateToTopic = navigateToTopic,
//            )
        }
        nestedGraphs()
    }
}