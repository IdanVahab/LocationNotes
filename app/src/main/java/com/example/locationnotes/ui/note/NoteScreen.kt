package com.example.locationnotes.ui.note

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun NoteScreen(
    navController: NavController,
    noteId: String,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity
    val permission = Manifest.permission.ACCESS_FINE_LOCATION

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        val shouldExplain = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

        if (!isGranted) {
            if (shouldExplain) {
                Toast.makeText(context, "Location permission is needed to get your location", Toast.LENGTH_LONG).show()
            }
            locationPermissionLauncher.launch(permission)
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGpsEnabled) {
            Toast.makeText(context, "Please enable GPS to get your location", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(noteId, locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            viewModel.loadNote(noteId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId.isEmpty()) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.hasUnsavedChanges()) {
                            showExitDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }

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
                    viewModel.saveNote(
                        onSuccess = {
                            Toast.makeText(context, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onError = {
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        }
                    )
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
                label = { Text("Title") },
                isError = uiState.titleError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.body,
                onValueChange = { viewModel.updateBody(it) },
                label = { Text("Content") },
                isError = uiState.bodyError,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("📍 Location: ${uiState.location}", style = MaterialTheme.typography.bodySmall)
            Text("🕒 Date: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(uiState.date)}")

            Spacer(modifier = Modifier.height(96.dp))
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
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Confirmation") },
            text = { Text("Are you sure you want to delete this note?") }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Unsaved changes") },
            text = { Text("You have unsaved changes. What would you like to do?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveNote(
                        onSuccess = {
                            Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show()
                            showExitDialog = false
                            navController.popBackStack()
                        },
                        onError = {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Text("Save and Exit")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showExitDialog = false
                    }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        showExitDialog = false
                        navController.popBackStack()
                    }) {
                        Text("Exit without Saving")
                    }
                }
            }
        )
    }

    BackHandler {
        if (viewModel.hasUnsavedChanges()) {
            showExitDialog = true
        } else {
            navController.popBackStack()
        }
    }
}