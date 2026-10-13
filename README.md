# Hävn — Medication Reminder

A local-first medication organizer for Android. No account, no servers, no
tracking, and no `INTERNET` permission — your medication history stays on your
phone.

Built with Jetpack Compose, Room, WorkManager, Hilt and Glance.

---

## What it does

- **Today** — the day's doses, grouped by part of day, with a 3D pill organizer
  as the hero rather than a stack of cards.
- **Organizer** — an interactive model of the physical object, four
  compartments matching morning / afternoon / evening / night.
- **Progress** — 30-day adherence as a trend chart and a calendar.
- **Reminders** — the schedule, notification permission state, and how alerts
  behave. Dose reminders carry *Mark taken* and *In 15 min* actions, so a dose
  can be logged without opening the app.
- **Profiles** — several people can share one device; each keeps its own
  medications and history.
- **Backup** — export everything as a single JSON file to Drive, Files, email
  or anywhere else you choose. Restore is additive and never overwrites what is
  already there.

## Build

Requires JDK 17+ and the Android SDK (compileSdk 36).

```bash
./gradlew assembleDebug
```

The debug build uses AGP's standard debug key and installs as
`com.havnmeds.app.debug`, so it can sit alongside a release install.

### Release builds

Release signing is read from `keystore.properties` (gitignored) or from the
environment. Copy the template and fill it in:

```bash
cp keystore.properties.example keystore.properties
```

On CI, set `HAVN_KEYSTORE`, `HAVN_KEYSTORE_PASSWORD`, `HAVN_KEY_ALIAS` and
`HAVN_KEY_PASSWORD` as secrets instead.

**If no signing config is found, `assembleRelease` produces an unsigned APK.**
That is deliberate. This project previously signed release builds with the
Android debug keystore, whose password is the literal string `android` and is
public knowledge — meaning anyone could modify the APK, re-sign it with the
same key, and Android would accept it as a legitimate update. Failing to sign
is the safe outcome; silently signing with a known key is not.

## Privacy and data

- The app declares **no network permission**. It cannot phone home.
- Data lives in a Room database in the app's private storage.
- **Android backup is off** (`allowBackup="false"`, everything excluded in
  `data_extraction_rules.xml`). Every install starts clean — no profile, name or
  history carries over from a previous install. The in-app export is the only
  way to move data.
- The app cache and WebView cache are cleared on every cold start.
- Exports are written to `cacheDir/exports` and handed to the system share
  sheet. The `FileProvider` is scoped to that one directory, so no other app
  can reach the database through it.

## Project layout

```
app/src/main/
├── java/com/havn/app/
│   ├── audio/             Sound and chime management
│   ├── data/              Room database, DataStore, session, backup manager
│   ├── di/                 Dagger Hilt dependency injection modules
│   ├── domain/model/      Medication, DoseLog, TodayDose, DayPeriod
│   ├── notifications/     Alarm and notification scheduling, channels, actions
│   ├── ui/
│   │   ├── theme/         Material 3 design tokens, typography, motion
│   │   ├── components/    Shared UI component library and native 3D-perspective Organizer Canvas
│   │   ├── navigation/    Session-gated NavGraph and scaffold
│   │   └── screens/       Today, Organizer, Progress, Reminders, Settings, Add/Edit Meds…
│   └── widget/            Android Glance home-screen app widget
└── res/                   Vector drawables, custom adaptive icon, typography, light/dark themes
```

## Architecture & Technology

- **UI Framework**: 100% Declarative Jetpack Compose + Material 3
- **Organizer**: Custom perspective canvas rendering with animated hinged lids and dosage pill specimens
- **Persistence**: Offline local-first SQLite via Room 2.6.x (zero cloud tracking or data transfer)
- **Dependency Injection**: Dagger Hilt
- **Background Tasks & Alarms**: Android AlarmManager, BroadcastReceivers, and WorkManager
- **App Widget**: Android Glance AppWidget with palette adaptation
- **Backup & Restore**: Portable JSON export/restore through Android FileProvider and system share sheet

