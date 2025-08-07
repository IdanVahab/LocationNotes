# Location Notes – Android App

An Android application that allows users to create notes based on their current location. Users can view, edit and delete notes either from a list or on a map interface.

## Features

- Firebase Authentication – login and signup support
- Create notes with title, content, timestamp and location
- List and map views for displaying notes
- View notes as markers on Google Maps
- Support for current location detection using Fused Location Provider
- Form validation and clear error feedback
- Navigation between screens using Jetpack Navigation
- Hilt for dependency injection
- Room for local storage of notes

## Tech Stack

- Jetpack Compose for UI
- Firebase Auth
- Room (SQLite abstraction)
- Google Maps Compose
- FusedLocationProviderClient
- Hilt (DI)
- MVVM Architecture


## Getting Started

1. Clone the repository
2. Open the project in Android Studio
3. Connect to Firebase via Firebase Assistant
4. Add `google-services.json` to the `app/` directory
5. Build and run on a device or emulator with location access enabled

## Notes

This application was developed as part of a coding assignment.  
All UI was built using Jetpack Compose and follows the MVVM pattern.  
Prompts were written by me and used with AI tools to help streamline development and maintain clean architecture.


