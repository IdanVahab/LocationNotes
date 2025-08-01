package com.example.locationnotes.ui.home

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
import com.example.locationnotes.ui.navigation.Screen

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🗺️ Map View - Coming Soon!")
                    }
                }
            }
        }
    }
}
