# 🎯 Tracktern — Android App

A production-quality Android application for tracking internship and job applications, built with modern Android development practices.

---

## 📱 Screenshots & Features

| Dashboard | Applications | Kanban Board |
|-----------|-------------|--------------|
| Stats overview, upcoming interviews, recent apps | Full list with filter | Drag-and-drop status columns |

| Add/Edit | Analytics | Resume Match |
|----------|-----------|--------------|
| Full form with reminders | Donut chart, funnel, conversions | NLP keyword scoring |

---

## 🏗️ Architecture

```
MVVM + Clean Architecture
│
├── UI Layer (Jetpack Compose)
│   ├── Screens (Dashboard, Applications, Kanban, AddEdit, Analytics, Search, Resume)
│   ├── Components (ApplicationCard, StatusChip)
│   ├── Navigation (NavGraph with sealed Screen routes)
│   └── Theme (Material 3, dark mode)
│
├── ViewModel Layer
│   ├── ApplicationViewModel — shared state, CRUD, search/filter
│   └── AddEditViewModel — form state, save/delete, notification scheduling
│
├── Domain Layer
│   ├── InternshipApplication (domain model)
│   ├── ApplicationStatus (enum: Applied, Interview, Offer, Rejected)
│   └── ApplicationStats (dashboard metrics)
│
└── Data Layer
    ├── Room Database (local SQLite via Room)
    ├── ApplicationDao (reactive Flow queries)
    ├── ApplicationEntity (DB entity + TypeConverters)
    └── ApplicationRepository (single source of truth)
```

---

## 🧩 Tech Stack

| Technology | Usage |
|-----------|-------|
| **Kotlin** | Primary language |
| **Jetpack Compose** | All UI (Material 3) |
| **Room** | Local SQLite database |
| **Hilt** | Dependency injection |
| **Navigation Compose** | Screen routing |
| **Kotlin Coroutines + Flow** | Async / reactive data |
| **WorkManager** | Background task scheduling |
| **AlarmManager** | Precise notification scheduling |
| **Material 3** | Design system + dark mode |

---

## 🚀 How to Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 26+ (Android 8.0 Oreo)
- JDK 17

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourname/InternshipTracker.git
   cd InternshipTracker
   ```

2. **Open in Android Studio**
   - File → Open → select the `InternshipTracker` folder

3. **Sync Gradle**
   - Android Studio will prompt to sync — click "Sync Now"
   - Or run: `./gradlew build`

4. **Run the app**
   - Connect a device (API 26+) or start an emulator
   - Click the green ▶️ Run button
   - Or: `./gradlew installDebug`

### First Launch
The app **automatically seeds 6 demo applications** on first launch so you can explore all features immediately without manual data entry.

---

## 📂 Project Structure

```
app/src/main/java/com/internshiptracker/
│
├── InternshipTrackerApp.kt          ← Application class (Hilt, notification channels)
├── MainActivity.kt                  ← Single activity, bottom navigation
│
├── data/
│   ├── local/
│   │   ├── InternshipDatabase.kt   ← Room database definition
│   │   ├── dao/
│   │   │   └── ApplicationDao.kt   ← All SQL queries (Flow-based)
│   │   └── entities/
│   │       └── ApplicationEntity.kt ← DB entity + domain converters
│   └── repository/
│       └── ApplicationRepository.kt ← Single data source for ViewModels
│
├── di/
│   └── AppModule.kt                ← Hilt module (DB, DAO providers)
│
├── domain/
│   └── model/
│       └── InternshipApplication.kt ← Domain models + enums
│
├── ui/
│   ├── components/
│   │   └── ApplicationCard.kt      ← Reusable card + StatusChip
│   ├── navigation/
│   │   └── Navigation.kt           ← NavHost + Screen sealed class
│   ├── screens/
│   │   ├── dashboard/DashboardScreen.kt
│   │   ├── applications/ApplicationsScreen.kt
│   │   ├── kanban/KanbanScreen.kt
│   │   ├── add_edit/AddEditScreen.kt
│   │   ├── search/SearchScreen.kt
│   │   ├── analytics/AnalyticsScreen.kt
│   │   └── resume/ResumeMatchScreen.kt
│   └── theme/
│       ├── Theme.kt                ← Material 3 theme + colors
│       └── Type.kt                 ← Typography
│
├── utils/
│   └── NotificationHelper.kt       ← AlarmManager + BroadcastReceivers
│
└── viewmodel/
    ├── ApplicationViewModel.kt      ← Shared app state
    └── AddEditViewModel.kt          ← Form state + save logic
```

---

## 🎨 Key Design Decisions

### Why MVVM?
- Clean separation between UI and business logic
- ViewModels survive configuration changes (screen rotations)
- Easy to unit test ViewModel logic in isolation

### Why Room + Flow?
- Flow queries are **reactive** — the UI automatically updates whenever data changes
- No manual refresh logic needed anywhere in the app
- Room handles all SQLite boilerplate safely

### Why Hilt?
- Eliminates manual dependency wiring
- Scopes singletons to the right lifecycle (Application, ViewModel)
- Works seamlessly with WorkManager and ViewModels

### Why sealed class for Navigation?
- Type-safe route definitions prevent string typos
- Arguments are encoded in the sealed class, not scattered
- Easy to find all routes at a glance

---

## ✨ Feature Highlights

### Dashboard
- Gradient hero banner with total count + success rate
- Scrollable status cards (Applied / Interview / Offer / Rejected)
- Upcoming interviews list from Room reactive query
- Recent applications preview

### Kanban Board
- Horizontally scrollable 4-column board
- Long-press + drag gesture to move cards between columns
- Status updates persist immediately via Repository
- Empty state with drop hint when dragging

### Analytics
- Animated donut chart (Canvas API) for status distribution
- Animated linear progress bars for funnel view
- Conversion rate calculations (Applied→Interview, Interview→Offer)
- Built-in **follow-up email generator** — no API needed

### Resume Match Score
- Tokenises both job description and resume
- Removes English stop words
- Counts matched vs missing JD keywords
- Animated circular score indicator (0–100%)
- Shows matched keywords (green) and missing keywords (red)
- Actionable improvement tips

### Notifications
- AlarmManager for exact scheduling
- Interview reminders fire 30 minutes before
- Follow-up reminders at the specified date/time
- Channels: `channel_interviews` and `channel_followups`
- Survives app kill (BroadcastReceiver)
- BootReceiver for reboot recovery

---

## 🧪 Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

---

## 🔧 Customisation

### Adding a new status
1. Add enum value to `ApplicationStatus` in `InternshipApplication.kt`
2. Add color in `Theme.kt`
3. Add case to `toColor()` in `ApplicationCard.kt`
4. Increment Room DB version and add migration

### Enabling Firebase Sync
1. Add `google-services.json` to `/app`
2. Add Firebase BOM + Firestore dependencies
3. Create `FirebaseRepository` implementing same interface as `ApplicationRepository`
4. Switch binding in `AppModule.kt`

---

## 📋 Sample Data

On first launch, 6 demo applications are inserted:

| Company | Role | Status |
|---------|------|--------|
| Google | Software Engineering Intern | Interview (with upcoming date) |
| Microsoft | Product Management Intern | Applied |
| Flipkart | Data Science Intern | **Offer** |
| Amazon | SDE Intern | Rejected |
| Swiggy | Backend Engineering Intern | Applied |
| Razorpay | Frontend Developer Intern | Interview |

---

## 📄 License

MIT License — free to use for academic and personal projects.

---

*Built with ❤️ using Kotlin, Jetpack Compose, and Material 3*
