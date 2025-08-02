package com.example.locationnotes.ui.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.locationnotes.data.model.Note
import com.example.locationnotes.ui.navigation.Screen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNotes by remember { mutableStateOf<List<Note>?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.Note.createRoute(null))
            }) {
                Icon(Icons.Default.Add, contentDescription = "New Note")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)) {

            Text(
                text = "ברוך הבא, ${uiState.userName} 👋",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("תצוגה: ")
                IconButton(onClick = { viewModel.toggleDisplayMode() }) {
                    Icon(
                        imageVector = if (uiState.displayMode == DisplayMode.LIST) Icons.Default.Place  else Icons.AutoMirrored.Filled.List,
                        contentDescription = "Toggle View"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.notes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("אין פתקים עדיין. 📝")
                    }
                }

                uiState.displayMode == DisplayMode.LIST -> {
                    LazyColumn {
                        items(uiState.notes) { note ->
                            Card(
                                onClick = {
                                    navController.navigate(Screen.Note.createRoute(note.id))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(note.title, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(note.body, maxLines = 2)
                                }
                            }
                        }
                    }
                }

                uiState.displayMode == DisplayMode.MAP -> {
                    val defaultLatLng = uiState.notes.firstOrNull()?.location
                        ?.split(",")?.mapNotNull { it.toDoubleOrNull() }
                        ?.takeIf { it.size == 2 }
                        ?.let { LatLng(it[0], it[1]) }
                        ?: LatLng(32.0853, 34.7818)

                    var selectedNotes by remember { mutableStateOf<List<Note>?>(null) }

                    GoogleMap(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp),
                        cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(defaultLatLng, 12f)
                        }
                    ) {
                        val groupedNotes = uiState.notes
                            .filter { it.location.isNotBlank() && it.location.contains(",") }
                            .groupBy { it.location.trim() }

                        groupedNotes.forEach { (locationStr, notesAtLocation) ->
                            val coords = locationStr
                                .split(",")
                                .mapNotNull { it.toDoubleOrNull() }

                            if (coords.size == 2) {
                                val latLng = LatLng(coords[0], coords[1])

                                Marker(
                                    state = MarkerState(position = latLng),
                                    title = if (notesAtLocation.size == 1)
                                        notesAtLocation.first().title
                                    else
                                        "${notesAtLocation.size} פתקים כאן",

                                    onClick = {
                                        selectedNotes = notesAtLocation
                                        true
                                    }
                                )
                            }
                        }
                    }

                    if (selectedNotes != null) {
                        AlertDialog(
                            onDismissRequest = { selectedNotes = null },
                            title = { Text("פתקים במקום הזה") },
                            text = {
                                Column {
                                    selectedNotes!!.forEach { note ->
                                        TextButton(onClick = {
                                            navController.navigate(Screen.Note.createRoute(note.id))
                                            selectedNotes = null
                                        }) {
                                            Text(note.title)
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { selectedNotes = null }) {
                                    Text("סגור")
                                }
                            }
                        )
                    }
                }


            }
        }
    }
}
