package com.example.locationnotes.ui.note

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.locationnotes.data.model.Note
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavController,
    noteId: String,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId.isEmpty()) "פתק חדש" else "עריכת פתק") }
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                if (noteId.isNotEmpty()) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                FloatingActionButton(onClick = {
                    viewModel.saveNote()
                    Toast.makeText(context, "הפתק נשמר", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }) {
                    Icon(Icons.Default.Done, contentDescription = "Save Note")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("כותרת") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.body,
                onValueChange = { viewModel.updateBody(it) },
                label = { Text("תוכן") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("📍 מיקום: ${uiState.location}", style = MaterialTheme.typography.bodySmall)
            Text("🕒 תאריך: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(uiState.date)}")
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote()
                    navController.popBackStack()
                }) {
                    Text("מחק")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ביטול")
                }
            },
            title = { Text("אישור מחיקה") },
            text = { Text("האם אתה בטוח שברצונך למחוק את הפתק הזה?") }
        )
    }

    BackHandler {
        navController.popBackStack()
    }
}
