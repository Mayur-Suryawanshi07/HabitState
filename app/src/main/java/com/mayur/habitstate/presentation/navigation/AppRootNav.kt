package com.mayur.habitstate.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppRootNav(
    navController: NavHostController
) {
    val isLoggedIn = remember {
        try {
            FirebaseAuth.getInstance().currentUser != null
        } catch (_: Exception) {
            false
        }
    }
    val initialDestination = if (isLoggedIn) Graphs.Main else Graphs.Auth

    NavHost(
        navController = navController,
        startDestination = initialDestination,
        enterTransition = { fadeIn(tween(150)) },
        exitTransition = { fadeOut(tween(150)) },
        popEnterTransition = { fadeIn(tween(150)) },
        popExitTransition = { fadeOut(tween(150)) }
    ) {
        authNavGraph(navController)
        mainNavGraph(navController)
    }
}
