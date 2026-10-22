package com.mayur.habitstate.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.mayur.habitstate.presentation.screens.main.component.Screen
import com.mayur.habitstate.presentation.screens.main.focus.FocusScreen
import com.mayur.habitstate.presentation.screens.main.home.HomeScreen
import com.mayur.habitstate.presentation.screens.main.profile.ProfileScreen

fun NavGraphBuilder.mainNavGraph(navController: NavController) {
    navigation<Graphs.Main>(startDestination = Routes.Home) {
        composable<Routes.Home> {
            HomeScreen(
                onNavigateToScreen = { screen ->
                    when (screen) {
                        Screen.Home -> { /* already on home */ }
                        Screen.Search -> navController.navigate(Routes.Search)
                        Screen.Profile -> navController.navigate(Routes.Profile)
                    }
                },
                onLogout = {
                    navController.navigate(Graphs.Auth) {
                        popUpTo<Graphs.Main> { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Search> {
            FocusScreen(
                onNavigateToScreen = { screen ->
                    when (screen) {
                        Screen.Home -> navController.navigate(Routes.Home) {
                            popUpTo<Routes.Home> { saveState = true }
                            launchSingleTop = true
                        }
                        Screen.Search -> { /* already on focus */ }
                        Screen.Profile -> navController.navigate(Routes.Profile) {
                            popUpTo<Routes.Home> { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable<Routes.Profile> {
            ProfileScreen(
                onNavigateToScreen = { screen ->
                    when (screen) {
                        Screen.Home -> navController.navigate(Routes.Home) {
                            popUpTo<Routes.Home> { saveState = true }
                            launchSingleTop = true
                        }
                        Screen.Search -> navController.navigate(Routes.Search) {
                            popUpTo<Routes.Home> { saveState = true }
                            launchSingleTop = true
                        }
                        Screen.Profile -> { }
                    }
                },
                onLogout = {
                    navController.navigate(Graphs.Auth) {
                        popUpTo<Graphs.Main> { inclusive = true }
                    }
                }
            )
        }
    }
}
