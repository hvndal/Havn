# Hävn — Developer & Agent Instructions (GEMINI.md)

Welcome to **Hävn**, a premium Scandinavian-minimalist medication organiser and reminder app built natively with modern Android Jetpack Compose.

## Project Overview

- **Package / Application ID**: `com.havnmeds.app`
- **Namespace**: `com.havn.app`
- **Target SDK**: API 36 (Android 16 / Upside Down Cake +)
- **Min SDK**: API 26 (Android 8.0 Oreo)
- **Version**: `1.0.2` (versionCode `3`)
- **UI Framework**: 100% Declarative Jetpack Compose + Material 3 + Custom Organiser Shader Canvas
- **Database**: Room 2.6.x (offline local-first SQLite, zero cloud data transfer)
- **DI**: Dagger Hilt
- **Widget**: Android Glance AppWidget (`HavnWidget`)

---

## Folder Structure

```
Havn/
├── START-HERE.md          # Quick start guide for building and publishing
├── GEMINI.md              # Project instructions for AI pair programmers & agents
├── AGENTS.md              # Duplicate pointer for multi-agent workflows
├── build-release.sh       # One-command automated build script for Linux/macOS/Codespaces
├── app/                   # Android native app source (Compose, Room, Hilt, Glance)
│   ├── src/main/
│   │   ├── java/com/havn/app/     # Native Kotlin source code
│   │   ├── assets/organizer/      # Three.js 3D organizer model
│   │   └── res/                   # Drawables, icons, layout, themes
├── play-store/            # Google Play Store publishing assets
│   ├── graphics/          # 512×512 app icon, 1024×500 feature graphic, screenshots/
│   └── listing/           # Store descriptions, privacy policy, Play Console answers
└── release-output/        # Output directory for production .aab and mapping files
```

---

## Build Commands

### On Windows (PowerShell / Command Prompt)

```powershell
# Set JDK 17 (from Android Studio JBR)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"

# Build release bundle (.aab)
.\gradlew.bat bundleRelease

# Build release APK (.apk)
.\gradlew.bat assembleRelease
```

### On Linux / macOS / GitHub Codespaces

```bash
bash build-release.sh
```

---

## Google Play Publishing Checklist

1. **Upload Artifact**: `release-output/Havn-1.0.2.aab`
2. **Graphics**:
   - App Icon: `play-store/graphics/icon-512.png`
   - Feature Graphic: `play-store/graphics/feature-graphic-1024x500.png`
   - Phone Screenshots: `play-store/graphics/screenshots/*.PNG`
3. **Store Listing**: Refer to `play-store/listing/store-listing.md`
4. **Privacy Policy**: Refer to `play-store/listing/privacy-policy.md`
5. **Data Safety Declarations**: Refer to `play-store/listing/play-console-answers.md`
