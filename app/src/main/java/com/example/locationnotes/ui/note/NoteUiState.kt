package com.example.locationnotes.ui.note

data class NoteUiState(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val date: Long = System.currentTimeMillis(),
    val location: String = ""
)
