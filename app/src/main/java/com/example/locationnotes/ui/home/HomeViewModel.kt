package com.example.locationnotes.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationnotes.data.repository.NoteRepository
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.CameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DisplayMode {
    LIST, MAP
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    val cameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(
            LatLng(32.0853, 34.7818),
            12f
        )
    )

    private val TAG = "HomeViewModel"

    init {
        loadUserName()
        loadNotes()
    }

    private fun loadNotes() {
        _uiState.update { it.copy(isLoading = true, errorLoading = false) }
        Log.d(TAG, "Loading notes...")

        viewModelScope.launch {
            try {
                noteRepository.getNotes()
                    .onEach { notes ->
                        Log.d(TAG, "Fetched ${notes.size} notes")
                        _uiState.update {
                            it.copy(
                                notes = notes,
                                isLoading = false,
                                errorLoading = false
                            )
                        }
                    }
                    .catch { error ->
                        Log.e(TAG, "Failed to load notes: ${error.message}", error)
                        _uiState.update {
                            it.copy(isLoading = false, errorLoading = true)
                        }
                    }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading notes: ${e.message}")
                _uiState.update {
                    it.copy(isLoading = false, errorLoading = true)
                }
            }
        }
    }

    fun retryLoadNotes() {
        Log.d(TAG, "Retrying note load...")
        loadNotes()
    }

    private fun loadUserName() {
        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName ?: user?.email?.substringBefore('@') ?: "User"
        Log.d(TAG, "Loaded user name: $name")
        _uiState.update {
            it.copy(userName = name)
        }
    }

    fun toggleDisplayMode() {
        val current = _uiState.value.displayMode
        val next = if (current == DisplayMode.LIST) DisplayMode.MAP else DisplayMode.LIST
        Log.d(TAG, "Toggling display mode to: $next")
        _uiState.update {
            it.copy(displayMode = next)
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        Log.d(TAG, "Logging out user")
        FirebaseAuth.getInstance().signOut()
        onLogoutComplete()
    }
}
