package com.lcl.lclmeasurementtool.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.lcl.lclmeasurementtool.MainActivityViewModel
import com.lcl.lclmeasurementtool.ui.HomeRoute

const val homeNavigationRoute = "home_route"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(homeNavigationRoute, navOptions)
}

fun NavGraphBuilder.homeScreen(isOffline: Boolean, mainViewModel: MainActivityViewModel) {
    composable(route = homeNavigationRoute) {
        HomeRoute(isOffline, mainViewModel)
    }
}