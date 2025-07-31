package com.example.locationnotes.ui.auth


data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLogin: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
