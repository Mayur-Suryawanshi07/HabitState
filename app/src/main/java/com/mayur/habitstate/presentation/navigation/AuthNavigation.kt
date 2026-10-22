package com.mayur.habitstate.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.mayur.habitstate.presentation.screens.auth.forgotpasswordscreen.ForgotPasswordScreen
import com.mayur.habitstate.presentation.screens.auth.loginscreen.LogInScreen
import com.mayur.habitstate.presentation.screens.auth.signupscreen.SignUpScreen

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation<Graphs.Auth>(startDestination = Routes.Login) {
        composable<Routes.Login> {
            LogInScreen(
                onNavigateToSignup = {
                    navController.navigate(Routes.Signup)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.ForgotPassword)
                },
                onNavigateToHome = {
                    navController.navigate(Graphs.Main) {
                        popUpTo<Graphs.Auth> { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Signup> {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Graphs.Main) {
                        popUpTo<Graphs.Auth> { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.ForgotPassword> {
            ForgotPasswordScreen(
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}
