# Hävn — Complete Google Play Release Package

Everything needed to build, test, and publish Hävn to the Google Play Store is organized right here in this folder.

---

## Folder Map

```
Havn/
├── START-HERE.md          ← This guide
├── GEMINI.md / AGENTS.md  ← AI assistant & agent documentation
├── build-release.sh       ← One-command build script (Linux/macOS/Codespaces)
├── app/ …                 ← Fixed Native Android app (v1.1.0, Target SDK 35)
├── play-store/
│   ├── graphics/          ← icon-512.png, feature-graphic-1024×500.png, screenshots/
│   └── listing/           ← Store text, privacy policy, Play Console answers
└── release-output/        ← Your production .aab appears here after the build
```

---

## Quick Steps to Release

### 1. Build the Release Bundle (`.aab`)
- **Windows (PowerShell)**:
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
  $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
  .\gradlew.bat bundleRelease
  ```
- **Linux / macOS / GitHub Codespaces**:
  ```bash
  bash build-release.sh
  ```
- The resulting `.aab` file will be copied into `release-output/`.

### 2. Prepare Store Graphics
All assets are ready in `play-store/graphics/`:
- **App Icon**: `play-store/graphics/icon-512.png` (512×512 PNG)
- **Feature Graphic**: `play-store/graphics/feature-graphic-1024x500.png` (1024×500 PNG)
- **Screenshots**: High-resolution screenshots are ready in `play-store/graphics/screenshots/`

### 3. Copy-Paste Store Details & Policy
In `play-store/listing/`:
- `store-listing.md`: Short description (80 chars) & Full description (up to 4000 chars)
- `privacy-policy.md`: Zero-data disclosure privacy policy text
- `play-console-answers.md`: Data safety questions, target audience, and content ratings
