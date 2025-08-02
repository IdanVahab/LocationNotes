package com.example.locationnotes.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.locationnotes.ui.navigation.Screen

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val TAG = "AuthScreen"
    val state by viewModel.uiState.collectAsState()

    Log.d(TAG, "Rendering AuthScreen. isLogin = ${state.isLogin}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (state.isLogin) "Login" else "Sign Up",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = {
                Log.d(TAG, "Email field changed: $it")
                viewModel.onEmailChanged(it)
            },
            label = { Text("Email") },
            isError = state.emailError,
            supportingText = {
                if (state.emailError) {
                    Text("Please enter a valid email", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = {
                Log.d(TAG, "Password field changed")
                viewModel.onPasswordChanged(it)
            },
            label = { Text("Password") },
            isError = state.passwordError,
            supportingText = {
                if (state.passwordError) {
                    Text("Password must be at least 6 characters", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (!state.isLogin) {
            OutlinedTextField(
                value = state.displayName,
                onValueChange = { viewModel.onDisplayNameChanged(it) },
                label = { Text("Name") },
                isError = state.displayNameError,
                supportingText = {
                    if (state.displayNameError) {
                        Text("Please enter your name", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = {
                    Log.d(TAG, "Confirm password field changed")
                    viewModel.onConfirmPasswordChanged(it)
                },
                label = { Text("Confirm Password") },
                isError = state.confirmPasswordError,
                supportingText = {
                    if (state.confirmPasswordError) {
                        Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }


        if (state.errorMessage != null) {
            Log.w(TAG, "Error message shown: ${state.errorMessage}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                Log.d(TAG, "Authentication button clicked. isLogin = ${state.isLogin}")
                viewModel.authenticate {
                    Log.d(TAG, "Authentication successful. Navigating to Home screen.")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text(if (state.isLogin) "Login" else "Sign Up")
        }

        TextButton(onClick = {
            Log.d(TAG, "Toggle mode button clicked")
            viewModel.toggleMode()
        }) {
            Text(if (state.isLogin) "Don't have an account? Sign Up" else "Already have an account? Login")
        }
    }
}
