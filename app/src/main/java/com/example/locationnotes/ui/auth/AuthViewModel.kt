package com.example.locationnotes.ui.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(com.example.locationnotes.ui.auth.AuthUiState())
    val uiState: StateFlow<com.example.locationnotes.ui.auth.AuthUiState> = _uiState

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isLogin = !_uiState.value.isLogin,
            errorMessage = null
        )
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    fun onPasswordChanged(newPass: String) {
        _uiState.value = _uiState.value.copy(password = newPass)
    }

    fun onConfirmPasswordChanged(newConfirm: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = newConfirm)
    }

    fun authenticate(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.email.isBlank() || state.password.length < 6) {
            _uiState.value = state.copy(errorMessage = "יש להזין אימייל וסיסמה תקינה")
            return
        }

        if (!state.isLogin && state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "הסיסמאות אינן תואמות")
            return
        }

        _uiState.value = state.copy(isLoading = true)

        viewModelScope.launch {
            if (state.isLogin) {
                login(state.email, state.password, onSuccess)
            } else {
                signup(state.email, state.password, onSuccess)
            }
        }
    }

    private fun login(email: String, password: String, onSuccess: () -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (it.isSuccessful) onSuccess()
                else _uiState.value = _uiState.value.copy(errorMessage = "שגיאה בהתחברות")
            }
    }

    private fun signup(email: String, password: String, onSuccess: () -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (it.isSuccessful) onSuccess()
                else _uiState.value = _uiState.value.copy(errorMessage = "שגיאה בהרשמה")
            }
    }
}
