package com.example.locationnotes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.locationnotes.ui.auth.AuthScreen
import com.example.locationnotes.ui.home.HomeScreen
import com.example.locationnotes.ui.note.NoteScreen
import com.example.locationnotes.ui.splash.SplashScreen


@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Auth.route) { AuthScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }

        composable("note/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
            NoteScreen(navController = navController, noteId = noteId)
        }

    }

}
