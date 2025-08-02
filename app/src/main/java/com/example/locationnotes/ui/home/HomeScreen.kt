package com.example.locationnotes.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.locationnotes.data.model.Note
import com.example.locationnotes.ui.navigation.Screen
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNotes by remember { mutableStateOf<List<Note>?>(null) }
    val context = LocalContext.current
    var isFirstMapLoad by remember { mutableStateOf(true) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        val shouldExplain = androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
            (context as android.app.Activity), permission
        )

        if (!isGranted) {
            if (shouldExplain) {
                Toast.makeText(context, "Location permission is needed to show notes on the map", Toast.LENGTH_LONG).show()
            }
            locationPermissionLauncher.launch(permission)
        } else {
            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!isGpsEnabled) {
                Toast.makeText(context, "GPS is disabled. Please enable it.", Toast.LENGTH_LONG).show()
            } else {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val latLng = LatLng(location.latitude, location.longitude)
                            viewModel.cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 14f)
                            Toast.makeText(context, "Moved to your location 📍", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Couldn't get current location", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Location error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, ${uiState.userName} 👋") },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Note.createRoute(null)) },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Note")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("View Mode: ")
                IconButton(onClick = { viewModel.toggleDisplayMode() }) {
                    Icon(
                        imageVector = if (uiState.displayMode == DisplayMode.LIST)
                            Icons.Default.Place
                        else
                            Icons.AutoMirrored.Filled.List,
                        contentDescription = "Toggle View"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.errorLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Failed to load notes. Check your internet.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.retryLoadNotes() }) {
                                Text("Try Again")
                            }
                        }
                    }
                }

                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.notes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notes yet. 📝")
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
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.body,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                uiState.displayMode == DisplayMode.MAP -> {
                    val cameraPositionState = viewModel.cameraPositionState
                    val firstLatLng = uiState.notes.firstOrNull()?.location
                        ?.split(",")?.mapNotNull { it.toDoubleOrNull() }
                        ?.takeIf { it.size == 2 }
                        ?.let { LatLng(it[0], it[1]) }

                    LaunchedEffect(uiState.displayMode) {
                        if (uiState.displayMode == DisplayMode.MAP && isFirstMapLoad) {
                            firstLatLng?.let {
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 12f)
                            }
                            isFirstMapLoad = false
                        }
                    }


                    val notesWithLocation = uiState.notes.filter {
                        it.location.isNotBlank() && it.location.contains(",")
                    }

                    if (notesWithLocation.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No notes with location.")
                        }
                    } else {
                        Box(Modifier.fillMaxSize()) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState
                            ) {
                                val groupedNotes = notesWithLocation.groupBy { it.location.trim() }

                                groupedNotes.forEach { (locationStr, notesAtLocation) ->
                                    val coords = locationStr.split(",").mapNotNull { it.toDoubleOrNull() }
                                    if (coords.size == 2) {
                                        val latLng = LatLng(coords[0], coords[1])
                                        Marker(
                                            state = MarkerState(position = latLng),
                                            title = if (notesAtLocation.size == 1)
                                                notesAtLocation.first().title
                                            else "${notesAtLocation.size} notes here",
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
                                    title = { Text("Notes at this location") },
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
                                            Text("Close")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
