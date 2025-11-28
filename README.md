# RFiDaTTen - RFID Attendance Recording App

A comprehensive Android application for recording attendance using RFID cards, built with Jetpack Compose and modern Android architecture.

## Features

### 📇 Card Management
- Register RFID cards with unique card numbers
- Create and manage user profiles (name, age, birthday, email)
- Edit card information and profiles
- Delete cards with cascading profile cleanup

### 📅 Session Management
- Create attendance sessions with custom names and time slots
- Select specific participants or allow all registered cards
- Start/stop session controls
- View active sessions and session history
- Export attendance records to CSV

### 🔍 RFID Scanning
- Simulated RFID scanning for testing
- Real-time scan validation:
  - Checks if card is registered
  - Verifies session is active
  - Validates participant authorization
  - Prevents duplicate scans
- Animated success/failure notifications
- Live attendance count updates

### 📊 Export & Reporting
- CSV export functionality
- Attendance records include:
  - Session metadata
  - Card numbers
  - Profile information
  - Scan timestamps
- Save to Downloads folder
- Share exported files

## Technology Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **Navigation**: Navigation Compose
- **Async**: Kotlin Coroutines & Flow
- **Build**: Gradle with KSP

## Project Structure

```
app/src/main/java/com/example/rfidatten/
├── data/
│   ├── entity/          # Room entities
│   ├── dao/             # Data Access Objects
│   ├── repository/      # Repository layer
│   └── AppDatabase.kt   # Database singleton
├── viewmodel/           # ViewModels with StateFlow
├── ui/
│   ├── screen/          # Compose screens
│   ├── navigation/      # Navigation setup
│   └── theme/          # Material 3 theme
└── util/               # Utility classes (Export, etc.)
```

## Setup & Installation

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26 or higher
- Kotlin 2.0.21

### Building the App

1. Clone the repository:
```bash
git clone https://github.com/tikiya99/RFiDaTTen.git
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Build and run on your device or emulator

## Usage Guide

### 1. Register Cards
1. Open **Card Manager** from the main screen
2. Tap the **+** button
3. Enter card details and profile information
4. Save the card

### 2. Create Session
1. Navigate to **Sessions**
2. Go to **Create** tab
3. Enter session name, start time, and end time
4. (Optional) Select specific participants
5. Create the session

### 3. Record Attendance
1. From **Sessions → Current**, start a session
2. Return to main screen
3. Enter card numbers and scan
4. View real-time attendance updates

### 4. Export Data
1. After a session ends, go to **Sessions → Past**
2. Select a session
3. Tap **Export Attendance**
4. Find the CSV file in Downloads folder

## Database Schema

### Tables
- **profiles**: User profile information
- **cards**: RFID card records linked to profiles
- **sessions**: Attendance session details
- **attendance**: Individual scan records
- **session_participants**: Junction table for participant selection

## RFID Hardware Integration

The current implementation uses **simulated scanning** for testing. For production use:

1. Identify your RFID reader type (USB/Bluetooth/NFC)
2. Add appropriate SDK/library
3. Replace `MainViewModel.simulateScan()` with hardware communication
4. Add necessary permissions

### Example Integration Points
- **USB**: Use `UsbManager` API
- **Bluetooth**: Use `BluetoothAdapter` API  
- **NFC**: Use Android NFC API

## Screenshots

> App includes:
> - Main Dashboard with active session display
> - Card Manager with list view
> - Profile editor with edit mode
> - Sessions screen with three tabs
> - Animated scan notifications

## Dependencies

```kotlin
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Navigation
implementation("androidx.navigation:navigation-compose:2.8.4")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

// Material Icons
implementation("androidx.compose.material:material-icons-extended:1.7.5")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
```

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## License

This project is open source and available under the MIT License.

## Future Enhancements

- [ ] Actual RFID hardware integration
- [ ] Cloud synchronization
- [ ] Admin authentication
- [ ] Excel export format
- [ ] Email reports
- [ ] Backup/restore functionality
- [ ] Multi-language support
- [ ] Dark mode customization

## Contact

For questions or support, please open an issue on GitHub.

---

**Built with ❤️ using Jetpack Compose**
