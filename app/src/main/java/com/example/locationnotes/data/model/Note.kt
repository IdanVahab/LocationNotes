package com.example.locationnotes.data.model

import com.example.locationnotes.data.local.NoteEntity

data class Note(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val date: Long = System.currentTimeMillis(),
    val location: String = ""
)

fun NoteEntity.toNote(): Note {
    return Note(
        id = id.toString(),
        title = title,
        body = body,
        date = date,
        location = location
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id.toLongOrNull() ?: 0L,
        title = title,
        body = body,
        date = date,
        location = location
    )
}
