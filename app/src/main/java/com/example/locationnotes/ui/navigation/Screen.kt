package com.example.locationnotes.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Note : Screen("note/{noteId}")
}
