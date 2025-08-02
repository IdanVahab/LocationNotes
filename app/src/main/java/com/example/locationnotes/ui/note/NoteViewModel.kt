package com.example.locationnotes.ui.note

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationnotes.data.model.Note
import com.example.locationnotes.data.repository.NoteRepository
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import java.io.IOException

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val app: Application,
    private val repository: NoteRepository
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState
    private val TAG = "NoteViewModel"
    private var originalNote: Note? = null

    fun loadNote(noteId: String) {
        if (noteId.isEmpty()) {
            Log.d(TAG, "Creating new note - generating location...")
            getLocation()
            return
        }

        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            if (note != null) {
                originalNote = note
                _uiState.value = NoteUiState(
                    id = note.id,
                    title = note.title,
                    body = note.body,
                    date = note.date,
                    location = note.location
                )
                Log.i(TAG, "Loaded existing note: ${note.id}")
            } else {
                Log.w(TAG, "Note with ID $noteId not found.")
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        val original = originalNote
        val current = _uiState.value

        return original?.title != current.title ||
                original?.body != current.body ||
                original?.location != current.location
    }


    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun updateBody(value: String) {
        _uiState.value = _uiState.value.copy(body = value)
    }

    fun saveNote(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _uiState.value
        val hasTitleError = current.title.isBlank()
        val hasBodyError = current.body.isBlank()

        if (hasTitleError || hasBodyError) {
            _uiState.value = current.copy(
                titleError = hasTitleError,
                bodyError = hasBodyError
            )
            onError("Please fill in title and body.")
            return
        }

        viewModelScope.launch {
            val note = Note(
                id = current.id,
                title = current.title,
                body = current.body,
                date = current.date,
                location = current.location
            )

            try {
                if (note.id.isBlank()) {
                    repository.addNote(note)
                    Log.i(TAG, "Created new note: ${note.title}")
                } else {
                    repository.updateNote(note)
                    Log.i(TAG, "Updated note: ${note.id}")
                }
                onSuccess()
            } catch (e: IOException) {
                Log.e(TAG, "No internet connection", e)
                onError("No internet connection. Please try again later.")
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error while saving note", e)
                onError("Something went wrong. Please try again.")
            }

        }
    }


    fun deleteNote() {
        viewModelScope.launch {
            val id = _uiState.value.id
            repository.deleteNote(id)
            Log.w(TAG, "Deleted note: $id")        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val fused = LocationServices.getFusedLocationProviderClient(app)
        fused.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val locStr = "${location.latitude},${location.longitude}"
                _uiState.value = _uiState.value.copy(location = locStr)
                Log.d(TAG, "Fetched location: $locStr")
            } else {
                Log.w(TAG, "⚠️ Location is null!")
            }
        }.addOnFailureListener {
            Log.e(TAG, "❌ Failed to get location", it)
        }
    }

}
