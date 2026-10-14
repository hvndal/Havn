# Hävn — LLM & AI Pair Programmer Guide

Welcome! This document provides context, architectural constraints, conventions, and operational workflows for any AI/LLM assisting with development on the **Hävn** Android codebase.

---

## 1. Project & Identity

- **App Name**: Hävn (Medication Organizer & Reminder)
- **Application ID (Release)**: `com.havnmeds.app`
- **Application ID (Debug)**: `com.havnmeds.app.debug`
- **Package / Namespace**: `com.havn.app`
- **Current Version**: `versionCode = 21`, `versionName = "1.1.3"`
- **Target SDK**: `36` (Android 15 / 16 preview compliant)
- **Compile SDK**: `36`
- **Min SDK**: `26` (Android 8.0 Oreo)
- **Zero Cloud / Zero Internet**: The app declares **NO `android.permission.INTERNET`**. All data stays on the local device SQLite database. Never add network dependencies or network calls.

---

## 2. Core Tech Stack & Dependencies

All dependencies are centrally managed in `gradle/libs.versions.toml`:

| Library / Tool | Version | Notes |
| :--- | :--- | :--- |
| **Android Gradle Plugin (AGP)** | `8.10.1` | |
| **Kotlin** | `2.0.0` | Uses Kotlin Compose Compiler plugin (`org.jetbrains.kotlin.plugin.compose`) |
| **Compose BOM** | `2024.09.03` | Resolves `androidx.compose.ui:ui:1.7.3` |
| **Activity Compose** | `1.9.2` | |
| **AndroidX Lifecycle** | `2.8.6` | **MUST** be unified across all lifecycle artifacts (`runtime-ktx`, `viewmodel-compose`, `runtime-compose`) |
| **Navigation Compose** | `2.8.2` | |
| **Room** | `2.6.1` | SQLite local persistence with KSP |
| **Dagger Hilt** | `2.51.1` | Dependency Injection (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`) |
| **Android Glance** | `1.1.0` | AppWidget home-screen widget |
| **Robolectric** | `4.14.1` | Native graphics mode (`GraphicsMode.Mode.NATIVE`) for screenshot testing |

---

## 3. Critical Rules & Guardrails for LLMs

### Rule 1: LocalLifecycleOwner Import (Never Regress)
* ❌ **NEVER IMPORT**: `androidx.compose.ui.platform.LocalLifecycleOwner` (deprecated in Compose UI 1.7+, causes runtime `IllegalStateException: CompositionLocal LocalLifecycleOwner not present` with Lifecycle 2.8+).
* ✅ **ALWAYS USE**: `androidx.lifecycle.compose.LocalLifecycleOwner`.
* In `MainActivity.kt`, always explicitly pass `lifecycleOwner = this@MainActivity` to root `collectAsStateWithLifecycle()` calls so they never depend on dynamic `CompositionLocal` lookups.
* A Gradle task (`checkNoDeprecatedLifecycleOwner`) automatically runs on `preBuild` and fails the build if the deprecated import is reintroduced.

### Rule 2: Strict Lifecycle Version Alignment
* All `androidx.lifecycle:*` artifacts must resolve to the identical version (`2.8.6`). 
* Gradle has a custom `resolutionResult` hook in `app/build.gradle.kts` that will throw a build failure if any lifecycle dependency drifts.

### Rule 3: Zero Network / Strict Offline Privacy
* Do not add `INTERNET` permission to `AndroidManifest.xml`.
* Android auto-backup is disabled (`allowBackup="false"`).
* Backups are handled strictly through user-initiated JSON file export/import via `FileProvider`.

### Rule 4: Preserving Database Migrations
* The Room database is at schema version `3`.
* Migrations (`MIGRATION_1_2`, `MIGRATION_2_3`) are tested in `MigrationTest.kt`. If modifying entities, you must add an explicit migration and update the schema test.

---

## 4. Key Directory & Code Map

```
app/src/main/java/com/havn/app/
├── HavnApp.kt                     # Application class with @HiltAndroidApp & notification channel setup
├── audio/                         # In-app sound effects (lid opening, pill taken chimes)
├── data/
│   ├── db/                        # Room database (HavnDatabase, UserDao, MedicationDao, DoseLogDao)
│   ├── prefs/                     # DataStore user preferences (UserPreferences.kt)
│   ├── session/                   # SessionManager & SessionState (resolves active profile)
│   └── backup/                    # JSON export/import logic via Android FileProvider
├── di/                            # Hilt modules (DatabaseModule, etc.)
├── domain/model/                  # Core entities: Medication, DoseLog, TodayDose, DayPeriod
├── notifications/                 # HavnAlarmManager, AlarmReceiver, BootReceiver, NotificationHelper
├── ui/
│   ├── MainActivity.kt            # Single activity host, SplashScreen, HavnTheme, root collectAsStateWithLifecycle
│   ├── theme/                     # HavnTheme, HavnColors, HavnTypography, HavnMotion, HavnTokens
│   ├── components/                # HavnOrganizerView (custom canvas), HavnButton, HavnSurface, etc.
│   ├── navigation/                # HavnNavGraph & HavnScaffold
│   └── screens/
│       ├── home/                  # TodayScreen (Hero 3D organizer view & daily doses)
│       ├── organizer/             # OrganizerScreen (4-compartment model)
│       ├── progress/              # ProgressScreen (30-day adherence chart & calendar)
│       ├── reminders/             # RemindersScreen (dose alarms & permissions)
│       ├── settings/              # SettingsScreen (appearance, backup, profiles)
│       ├── addmed/                # AddMedicationScreen (medication wizard)
│       ├── managemeds/            # ManageMedicationsScreen (medication inventory)
│       └── onboarding/            # OnboardingScreen (first-time welcome)
└── widget/                        # HavnWidget (Glance AppWidget for home screen)
```

---

## 5. Build, Test & Release Commands

### Environment Setup (Windows PowerShell)
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
```

### Running Tests (Robolectric + Unit Tests)
```powershell
# Runs all unit tests and full-stack Robolectric UI walkthrough
.\gradlew.bat testDebugUnitTest

# Re-run and output screenshots to app/build/screenshots/
.\gradlew.bat testDebugUnitTest --rerun-tasks
```

### Building Release Artifacts
```powershell
# Build signed Android App Bundle (.aab)
.\gradlew.bat bundleRelease

# Build signed APK (.apk)
.\gradlew.bat assembleRelease
```

### Release Signing
Release builds require `keystore.properties` in the project root:
```properties
storeFile=release-output/KEEP-THIS-SAFE/havn-upload.jks
storePassword=havn2026
keyAlias=havn
keyPassword=havn2026
```
Outputs are generated in:
* Bundle: `release-output/Havn-1.1.3.aab`
* APK: `release-output/Havn-1.1.3.apk`
* Mapping: `release-output/mapping-1.1.3.txt`

---

## 6. How to Verify Any Changes
Before finishing a coding task, an LLM should run:
1. `.\gradlew.bat testDebugUnitTest` — Confirms no logic, database, or UI initialization regressions.
2. `.\gradlew.bat assembleRelease` — Confirms ProGuard/R8 code shrinking and native symbols succeed.
3. Check `git status` to ensure only intended files were touched.
