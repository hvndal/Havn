# Publishing Hävn to Google Play (v2.0.1 · `versionCode = 31`)

## 1. Ready-to-Upload Release Artifacts (`release-output/`)

All verified release artifacts for **Hävn `2.0.1` (`versionCode = 31`)** are located in [`release-output/`](release-output/):

| File | Purpose in Google Play Console |
|---|---|
| [`release-output/Havn-2.0.1-31-unsigned.aab`](release-output/Havn-2.0.1-31-unsigned.aab) | Unsigned Android App Bundle ready to be signed with your `havn-upload.jks` (`SHA1: 70:D6:F0:8C:F6:B4:6E:49:E3:A5:4E:4F:D7:ED:C0:06:6A:54:DE:4C`) |
| [`release-output/sign-with-upload-key.sh`](release-output/sign-with-upload-key.sh) | One-command helper script (`bash release-output/sign-with-upload-key.sh /path/to/havn-upload.jks havn`) that outputs `Havn-2.0.1-31-play-upload.aab` |
| [`release-output/Havn-2.0.1-31.aab`](release-output/Havn-2.0.1-31.aab) | Pre-signed verification AAB (passes `bundletool validate` and `bundletool build-apks`) |
| [`release-output/Havn-2.0.1-31-universal.apk`](release-output/Havn-2.0.1-31-universal.apk) | Zipaligned + v1/v2/v3 signed universal APK for direct device sideloading and QA |
| [`release-output/mapping-2.0.1-31.txt`](release-output/mapping-2.0.1-31.txt) | R8/ProGuard deobfuscation mapping file (also embedded inside the `.aab` at `BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`) |
| [`release-output/native-debug-symbols-2.0.1-31.zip`](release-output/native-debug-symbols-2.0.1-31.zip) | Native `.so` libraries archive (`<abi>/libandroidx.graphics.path.so`, 16 KB `0x4000` page-aligned across `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) |

---

## 2. Sign with Your Original Upload Key (`havn-upload.jks`)

Google Play App Signing requires updates to `com.havnmeds.app` to be signed with the exact upload key used for `versionCode = 30`:
- **Owner:** `CN=Herman Hundal, O=Mander, L=Mohali, C=IN`
- **SHA1:** `70:D6:F0:8C:F6:B4:6E:49:E3:A5:4E:4F:D7:ED:C0:06:6A:54:DE:4C`
- **SHA256:** `62:F8:51:D7:3A:8E:91:18:D7:05:81:56:51:A0:F4:E1:08:2F:30:B0:CF:DA:5E:08:E2:00:20:17:77:F6:89:3B`

Run:

```bash
bash release-output/sign-with-upload-key.sh /path/to/havn-upload.jks havn
```

This creates `release-output/Havn-2.0.1-31-play-upload.aab`, ready to drop directly into Google Play Console.

---

## 3. What Google Play Console Asks For During Release Upload

1. **App Bundle (`.aab`):**
   - Upload `Havn-2.0.1-31-play-upload.aab` (`versionCode = 31`, `versionName = "2.0.1"`, `compileSdk = 36`).
2. **R8 / ProGuard Deobfuscation Mapping File:**
   - **Automatically recognized** from inside the `.aab` (`BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`).
   - If you ever upload manually under **App bundle explorer → Downloads → ReTrace mapping file**, upload [`release-output/mapping-2.0.1-31.txt`](release-output/mapping-2.0.1-31.txt).
3. **Native Debug Symbols (`.zip`):**
   - `com.havnmeds.app` includes `libandroidx.graphics.path.so` from Google's Jetpack `androidx.graphics:graphics-path:1.0.1` artifact (which Google distributes pre-stripped).
   - If Play Console displays a non-blocking informational warning about unstripped native symbols for `libandroidx.graphics.path.so`, it is safe to proceed (or attach [`release-output/native-debug-symbols-2.0.1-31.zip`](release-output/native-debug-symbols-2.0.1-31.zip)).
   - All 4 ABIs (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) have `LOAD` segment alignment `0x4000` (**16 KB page alignment**), satisfying Android 15/16 (API 35/36) Play Console requirements.
4. **Release Name & Release Notes ("What's new"):**
   - **Release name:** `31 (2.0.1)`
   - **Release notes:** copy from [`fastlane/metadata/android/en-US/changelogs/31.txt`](fastlane/metadata/android/en-US/changelogs/31.txt).
5. **App Content & Declarations:**
   - **Privacy policy URL:** `https://mander.tech/havn/privacy` (or link to [`PRIVACY.md`](PRIVACY.md) / [`play-store/listing/privacy-policy.md`](play-store/listing/privacy-policy.md))
   - **Ads:** No
   - **Data safety:** No data collected, no data shared (100% local on-device storage)
   - **Health apps declaration:** Medication & pill reminder management; all records remain local on device
   - **Foreground service / Exact alarms (`USE_EXACT_ALARM` / `SCHEDULE_EXACT_ALARM`):** Core app functionality is time-critical medication dose reminders scheduled by the user
   - **Target audience:** 18+

---

## 4. Store Graphics & 87-Locale Metadata

- **Master English (`en-US`) ASO Pack:** [`play-store/listing/store-listing.md`](play-store/listing/store-listing.md)
- **87-Locale Translations:** [`fastlane/metadata/android/`](fastlane/metadata/android/) (validated via `python3 play-store/listing/sync_play_store_metadata.py --check`)
- **App Icon (512×512):** [`play-store/graphics/icon-512.png`](play-store/graphics/icon-512.png)
- **Feature Graphic (1024×500):** [`play-store/graphics/feature-graphic-1024x500.png`](play-store/graphics/feature-graphic-1024x500.png)
- **Device Screenshots (24 PNGs across 5 categories):**
  - Phone (`1080×1920`, 6 scenes): [`play-store/graphics/screenshots/phone/`](play-store/graphics/screenshots/phone/)
  - Flip Phone (`1080×2640`, 4 scenes): [`play-store/graphics/screenshots/flip/`](play-store/graphics/screenshots/flip/)
  - Foldable (`1812×2176`, 4 scenes): [`play-store/graphics/screenshots/foldable/`](play-store/graphics/screenshots/foldable/)
  - 7-Inch Tablet (`1200×1920`, 5 scenes): [`play-store/graphics/screenshots/tablet-7/`](play-store/graphics/screenshots/tablet-7/)
  - 10-Inch Tablet (`2560×1600`, 5 scenes): [`play-store/graphics/screenshots/tablet-10/`](play-store/graphics/screenshots/tablet-10/)
