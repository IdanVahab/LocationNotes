package com.example.locationnotes.data.repository

import com.example.locationnotes.data.local.NoteDao
import com.example.locationnotes.data.model.Note
import com.example.locationnotes.data.model.toEntity
import com.example.locationnotes.data.model.toNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { list ->
            list.map { it.toNote() }
        }
    }

    override suspend fun getNoteById(id: String): Note? {
        return noteDao.getNoteById(id.toLongOrNull() ?: return null)?.toNote()
    }

    override suspend fun addNote(note: Note) {
        noteDao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(noteId: String) {
        val idLong = noteId.toLongOrNull() ?: return
        val entity = noteDao.getNoteById(idLong) ?: return
        noteDao.deleteNote(entity)
    }
}
