# Hävn — Complete Google Play Release Package (`v2.0.1` · `versionCode = 31`)

Everything needed to verify, sign, and publish **Hävn `2.0.1` (`versionCode = 31`)** to the Google Play Store is organized in this repository.

---

## Folder Map

```
Havn/
├── START-HERE.md          ← Quick start guide for v2.0.1 (versionCode 31)
├── PUBLISHING.md          ← Google Play Console release, signing & declarations guide
├── GEMINI.md / AGENTS.md  ← AI assistant & agent documentation
├── fastlane/metadata/     ← 87 Google Play locales (title, short/full description, changelogs, en-US images)
├── play-store/
│   ├── graphics/          ← icon-512.png, feature-graphic-1024x500.png, screenshots/{phone,flip,foldable,tablet-7,tablet-10}/
│   ├── listing/           ← Master ASO store-listing.md, 87-locale batches, sync_play_store_metadata.py
│   └── screenshot-builder/← Exact-resolution multi-device screenshot renderer (build_all.js)
└── release-output/        ← Ready-to-upload v2.0.1 (versionCode 31) AAB, APK, R8 mapping & debug symbols
```

---

## Quick Steps to Release `v2.0.1` (`versionCode = 31`)

### 1. Sign `Havn-2.0.1-31-unsigned.aab` with Your Upload Key
All History tab crashes (`FigureCard` negative padding `IllegalArgumentException`, month-rollover `DateTimeException` in `DayMark`/`Ledger`/`weekOf`, and uncaught exceptions in `saveMonth`/`shareReport`) are fixed in the compiled `versionCode = 31` bundle in [`release-output/`](release-output/):

- **Sign with your original `havn-upload.jks` (`SHA1: 70:D6:F0:8C:F6:B4:6E:49:E3:A5:4E:4F:D7:ED:C0:06:6A:54:DE:4C`)**:
  ```bash
  bash release-output/sign-with-upload-key.sh /path/to/havn-upload.jks havn
  ```
- **Upload to Play Console**:
  - Bundle: `release-output/Havn-2.0.1-31-play-upload.aab` (or [`release-output/Havn-2.0.1-31.aab`](release-output/Havn-2.0.1-31.aab) for local/bundletool testing)
  - Standalone R8 Mapping File: [`release-output/mapping-2.0.1-31.txt`](release-output/mapping-2.0.1-31.txt) (also pre-embedded inside the `.aab`)
  - Native Symbols Archive: [`release-output/native-debug-symbols-2.0.1-31.zip`](release-output/native-debug-symbols-2.0.1-31.zip) (16 KB `0x4000` page-aligned)
  - Direct Test APK: [`release-output/Havn-2.0.1-31-universal.apk`](release-output/Havn-2.0.1-31-universal.apk)

### 2. Upload Store Graphics
All updated v2.0.1 assets are in [`play-store/graphics/`](play-store/graphics/):
- **App Icon (`512×512`)**: [`play-store/graphics/icon-512.png`](play-store/graphics/icon-512.png)
- **Feature Graphic (`1024×500`)**: [`play-store/graphics/feature-graphic-1024x500.png`](play-store/graphics/feature-graphic-1024x500.png)
- **Multi-Device Screenshots (24 PNGs)**:
  - Phone (`1080×1920`): [`play-store/graphics/screenshots/phone/`](play-store/graphics/screenshots/phone/) (mirrored at [`play-store/graphics/screenshots/`](play-store/graphics/screenshots/))
  - Flip Phone (`1080×2640`): [`play-store/graphics/screenshots/flip/`](play-store/graphics/screenshots/flip/)
  - Foldable (`1812×2176`): [`play-store/graphics/screenshots/foldable/`](play-store/graphics/screenshots/foldable/)
  - 7-Inch Tablet (`1200×1920`): [`play-store/graphics/screenshots/tablet-7/`](play-store/graphics/screenshots/tablet-7/)
  - 10-Inch Tablet (`2560×1600`): [`play-store/graphics/screenshots/tablet-10/`](play-store/graphics/screenshots/tablet-10/)

### 3. Copy-Paste Store Details, 87-Locale Metadata & Policy
- [`play-store/listing/store-listing.md`](play-store/listing/store-listing.md): ASO title options (≤ 30 chars), short description options (≤ 80 chars), full description, and v2.0.1 release notes
- [`fastlane/metadata/android/`](fastlane/metadata/android/): Ready-to-upload localized titles, short descriptions, and full descriptions for all 87 Google Play Console languages
- [`play-store/listing/privacy-policy.md`](play-store/listing/privacy-policy.md): Zero-data disclosure privacy policy text
- [`play-store/listing/play-console-answers.md`](play-store/listing/play-console-answers.md): Data safety questions, target audience, and content ratings
