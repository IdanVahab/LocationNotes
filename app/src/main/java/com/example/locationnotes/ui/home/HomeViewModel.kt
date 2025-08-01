package com.example.locationnotes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationnotes.data.model.Note
import com.example.locationnotes.data.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
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

    init {
        loadNotes()
        loadUserName()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            noteRepository.getNotes()
                .onEach { notes ->
                    _uiState.update { it.copy(notes = notes, isLoading = false) }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun loadUserName() {
        val user = FirebaseAuth.getInstance().currentUser
        _uiState.update {
            it.copy(userName = user?.displayName ?: user?.email?.substringBefore('@') ?: "User")
        }
    }

    fun toggleDisplayMode() {
        _uiState.update {
            it.copy(displayMode = if (it.displayMode == DisplayMode.LIST) DisplayMode.MAP else DisplayMode.LIST)
        }
    }
}
