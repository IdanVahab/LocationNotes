package com.example.locationnotes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.locationnotes.ui.auth.AuthScreen
import com.example.locationnotes.ui.home.HomeScreen
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavHost(navController: NavHostController) {
    val isAuthenticated = FirebaseAuth.getInstance().currentUser != null
    val startDestination = if (isAuthenticated) Screen.Home.route else Screen.Auth.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Auth.route) {
            AuthScreen(navController)
        }

        composable(Screen.Home.route) {
            HomeScreen()
        }
    }
}
