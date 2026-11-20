# HabitState 🎯

**HabitState** is a modern, minimal, clean-architecture Android application designed for intuitive habit tracking, consistency analytics, and distraction-free productivity using Focus Mode app blocking.

Built with **Kotlin**, **Jetpack Compose**, **Material 3**, **MVVM + Clean Architecture**, **Room Database**, and **Firebase Authentication**.

---

## 🌟 Key Features

### 1. 📊 Modern Home Dashboard
- **Header**: Current formatted date display (`Today, 11th Nov`), gold premium status pill, progress sharing, and quick settings access.
- **Weekly Calendar Strip**: Horizontally scrollable calendar strip highlighting completed, missed, and upcoming days with date selection.
- **Filters & Habit Score**: Filter habits by `All` or `Due`, and view overall real-time **Habit Score %** with a direct tap to launch the score screen.
- **Habit Cards**: Custom colored cards displaying habit title, streak count, quick progress notes, interactive checkmark toggle, and a **7-row historical consistency heatmap grid**.

### 2. 📈 Habit Score & Analytics Screen
- **Overall Score Arc Gauge**: Large semicircular gauge featuring 3 distinct color segments (Coral Red, Warm Yellow, Mint Green) and an indicator needle pointing to the overall score out of 100.
- **Per-Habit Gauges**: 2-column grid of individual habit score cards with mini gauges, completion percentages, and habit status.
- **Accurate Scoring**: Calculates score using actual completion logs against configured frequencies, excluding future and unscheduled dates.

### 3. ✍️ Simplified Habit Management
- **Customization**: Set Title, Optional Description, Color, Icon, and Frequency.
- **Flexible Frequency Rules**:
  - `Everyday`
  - `Specific Days of Week` (Mon-Sun selection)
  - `Specific Days of Month` (1-31 date selection)
  - `Some Days Per Period` (e.g. 3 days per 7 days)
- **Reminder Configuration**: Set exact reminder time with notification alarms.

### 4. 🧘 Focus Mode & App Blocker
- **App Blocking**: Select distracting apps (e.g. Instagram, YouTube) to block during focus sessions.
- **Accessibility Service**: `FocusAccessibilityService` monitors foreground applications and presents `FocusBlockedActivity` overlay when restricted apps are launched.
- **Break & Status Notifications**: Take 5-minute breaks or stop focus sessions via ongoing notification controls.

### 5. 🎨 Theme & Profile Preferences
- **Theme Modes**: Seamlessly switch between `System Default`, `Light Mode`, and `Dark Mode` with dynamic real-time app re-theming.
- **Profile Management**: Update display name and view streak statistics.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture (Data, Domain, Presentation)
- **State Management**: Kotlin `StateFlow` & `SharedFlow`
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) (SQLite) with KSP
- **Authentication**: [Firebase Auth](https://firebase.google.com/docs/auth)
- **Background Tasks**: AndroidX WorkManager & AlarmManager
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) & Fallback Secondary ViewModel Constructors

---

## 📁 Project Structure

```
HabitState
├── app/src/main/java/com/mayur/habitstate/
│   ├── data/                 # Data Layer (Room DB, DAOs, Repositories & Preferences)
│   ├── di/                   # Hilt Dependency Injection Modules
│   ├── domain/               # Domain Layer (Models, Repository Interfaces & Use Cases)
│   ├── presentation/         # Presentation Layer (Compose Screens, Navigation & ViewModels)
│   │   ├── navigation/       # Type-safe Jetpack Navigation Routes
│   │   └── screens/
│   │       ├── auth/         # Login, Registration & Password Recovery
│   │       └── main/         # Home, HabitScore, AddEditHabit, Focus & Profile
│   ├── service/              # System Accessibility Service for App Blocking
│   └── ui/theme/             # Material 3 ColorScheme & Typography Tokens
└── app/src/test/java/        # Unit Test Suites & Test Fakes
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java 17 or higher
- **Android SDK**: Compile SDK `35` / `37`, Min SDK `26` (Android 8.0+)
- **Device / Emulator**: Android 8.0+ (API 26+)

---

### Installation & Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/HabitState.git
   cd HabitState
   ```

2. **Firebase Configuration**
   - Place your `google-services.json` file inside the `app/` directory (`app/google-services.json`).
   - If testing locally without Firebase, authentication fallback handlers ensure zero build errors.

3. **Open in Android Studio**
   - Open Android Studio, select **Open**, and navigate to the `HabitState` root directory.
   - Allow Gradle to sync dependencies automatically.

4. **Build & Run**
   - Connect an Android device or launch an Emulator.
   - Run the app via **Run > Run 'app'** or terminal:
     ```bash
     ./gradlew :app:assembleDebug
     ```

---

## 🧪 Running Tests

To run the automated unit test suite (30+ unit tests covering UseCases, ViewModels, and Data persistence logic):

```bash
./gradlew :app:testDebugUnitTest
```

---

## 🔒 Permissions Required

For full Focus Mode and notification features, the application requests:

- `BIND_ACCESSIBILITY_SERVICE`: Enables application window state monitoring for app blocking.
- `SYSTEM_ALERT_WINDOW`: Displays the full-screen blocking overlay over restricted apps.
- `POST_NOTIFICATIONS`: Android 13+ status and reminder notifications.
- `SCHEDULE_EXACT_ALARM`: Android 12+ exact minute habit reminder alarms.

---

## 📝 License

This project is open source and available under the [MIT License](LICENSE).
