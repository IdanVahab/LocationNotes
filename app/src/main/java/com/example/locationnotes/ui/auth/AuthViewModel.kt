package com.example.locationnotes.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {


    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val TAG = "AuthViewModel"

    fun toggleMode() {
        Log.d(TAG, "Toggled mode: isLogin = ${!_uiState.value.isLogin}")
        _uiState.value = _uiState.value.copy(
            isLogin = !_uiState.value.isLogin,
            errorMessage = null,
            emailError = false,
            passwordError = false,
            confirmPasswordError = false
        )
    }

    fun onEmailChanged(newEmail: String) {
        Log.d(TAG, "Email updated: $newEmail")
        _uiState.value = _uiState.value.copy(
            email = newEmail,
            emailError = false
        )
    }

    fun onDisplayNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name, displayNameError = false)
    }

    fun onPasswordChanged(newPassword: String) {
        Log.d(TAG, "Password updated")
        _uiState.value = _uiState.value.copy(
            password = newPassword,
            passwordError = false
        )
    }

    fun onConfirmPasswordChanged(newConfirm: String) {
        Log.d(TAG, "Confirm password updated")
        _uiState.value = _uiState.value.copy(
            confirmPassword = newConfirm,
            confirmPasswordError = false
        )
    }

    fun authenticate(onSuccess: () -> Unit) {
        val state = _uiState.value

        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        val emailErr = state.email.isBlank() || !emailPattern.matcher(state.email).matches()
        val passErr = state.password.length < 6
        val confirmErr = !state.isLogin && state.password != state.confirmPassword
        val nameErr = !state.isLogin && state.displayName.isBlank()

        if (emailErr || passErr || confirmErr|| nameErr) {
            Log.w(TAG, "Validation failed → emailErr=$emailErr, passErr=$passErr, confirmErr=$confirmErr, nameErr=$nameErr")

            _uiState.value = state.copy(
                emailError = emailErr,
                passwordError = passErr,
                confirmPasswordError = confirmErr,
                displayNameError = nameErr,
                errorMessage = "Please fix the highlighted fields"
            )
            return
        }

        Log.d(TAG, "Authentication started: isLogin = ${state.isLogin}")
        _uiState.value = state.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            if (state.isLogin) {
                login(state.email, state.password, onSuccess)
            } else {
                signup(state.email, state.password, onSuccess)
            }
        }
    }

    private fun login(email: String, password: String, onSuccess: () -> Unit) {
        Log.d(TAG, "Attempting login for: $email")
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (it.isSuccessful) {
                    Log.d(TAG, "Login successful for: $email")
                    onSuccess()
                } else {
                    val errorMessage = when (val e = it.exception) {
                        is FirebaseAuthInvalidUserException -> "No user found with this email"
                        is FirebaseAuthInvalidCredentialsException -> "Incorrect password"
                        is FirebaseNetworkException -> "No internet connection. Please try again."
                        else -> "Login failed: ${e?.localizedMessage}"
                    }
                    Log.e(TAG, "Login failed: ${it.exception}")
                    _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
                }
            }
    }

    private fun signup(email: String, password: String, onSuccess: () -> Unit) {
        Log.d(TAG, "Attempting signup for: $email")

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Signup successful for: $email")

                    val user = firebaseAuth.currentUser
                    val name = _uiState.value.displayName

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d(TAG, "Display name set: $name")
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                onSuccess()
                            } else {
                                Log.e(TAG, "Failed to update display name: ${updateTask.exception}")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Failed to update display name: ${updateTask.exception?.localizedMessage}"
                                )
                            }
                        }

                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)

                    val errorMessage = when (val e = task.exception) {
                        is FirebaseAuthUserCollisionException -> "This email is already in use"
                        is FirebaseAuthWeakPasswordException -> "Password is too weak"
                        is FirebaseNetworkException -> "No internet connection. Please try again."
                        else -> "Signup failed: ${e?.localizedMessage}"
                    }

                    Log.e(TAG, "Signup failed: ${task.exception}")
                    _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
                }
            }
    }

}
