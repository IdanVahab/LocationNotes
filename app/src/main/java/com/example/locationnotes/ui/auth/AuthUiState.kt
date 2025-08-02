package com.example.locationnotes.ui.auth

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val displayNameError: Boolean = false,
    val isLogin: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Validation flags for visual feedback
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val confirmPasswordError: Boolean = false
)
