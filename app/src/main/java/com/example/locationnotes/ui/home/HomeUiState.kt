package com.example.locationnotes.ui.home

import com.example.locationnotes.data.model.Note

data class HomeUiState(
    val userName: String = "",
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
    val errorLoading: Boolean = false,
    val displayMode: DisplayMode = DisplayMode.LIST
)
