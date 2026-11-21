# Hävn — Google Play Store Multi-Language Listings (87 Locales)

Localized Google Play Store metadata is generated under `fastlane/metadata/android/<locale>/` for all **87 official Google Play Console languages and regional variants**:

- `title.txt` (≤ 30 characters)
- `short_description.txt` (≤ 80 characters)
- `full_description.txt` (≤ 4,000 characters, including localized medical disclaimer)

The canonical translation source files live in `play-store/listing/translations/batch*.json` and are validated/exported with:

```bash
python3 play-store/listing/sync_play_store_metadata.py          # regenerate fastlane/metadata/android/
python3 play-store/listing/sync_play_store_metadata.py --check  # validate locales & Play Console character limits
```

---

## Play Console & Medical Disclaimer Review Notes

1. **Medical Disclaimer Included in All 87 Locales**
   Every `full_description.txt` concludes with a localized translation of:
   > *"Hävn is a reminder tool and does not provide medical advice. Always follow your doctor's or pharmacist's instructions."*

2. **Recommended Native-Speaker Spot Check (Medical & Health Terminology)**
   All translations are AI-generated. Because Hävn falls under Google Play's **Health / Medical** category, prioritize native-speaker review of terms for *"dose"*, *"mark taken"*, *"snooze / in 15 min"*, *"medical advice"*, and *"pharmacist"*—especially for:
   - **RTL scripts**: `ar`, `iw-IL`, `fa`, `fa-IR`, `fa-AF`, `fa-AE`, `ur`
   - **No Play Console auto-translate fallback**: `hy-AM` (Armenian), `rm` (Romansh), `fil` (Filipino), `zu` (Zulu)
   - **South / Southeast Asian & African scripts**: `am`, `bn-BD`, `gu`, `hi-IN`, `kn-IN`, `km-KH`, `lo-LA`, `ml-IN`, `mr-IN`, `my-MM`, `ne-NP`, `pa`, `si-LK`, `sw`, `ta-IN`, `te-IN`
   - **Caucasus / Central Asian**: `az-AZ`, `ka-GE`, `kk`, `ky-KG`, `mn-MN`

3. **Google Play Locale Code Quirks**
   - Hebrew uses **`iw-IL`** (Play Console / Android legacy code, not `he`).
   - Norwegian uses **`no-NO`**.
   - Spanish uses **`es-ES`**, **`es-419`** (Latin America), and **`es-US`**.
   - Chinese uses **`zh-CN`**, **`zh-TW`**, and **`zh-HK`**.
   - `title.txt` and `short_description.txt` deliberately avoid promotional words (`free`, `no ads`, `#1`) per Google Play Metadata Policy; `free with no ads` is stated factually in `full_description.txt`.

4. **Synced with `30.aab` (v2.0.0 / versionCode 30)**
   - Updated all 87 localized descriptions (`batch1`..`batch6`) and `fastlane/metadata/android/` to reflect the actual v2.0.0 feature set: `Medicines` shelf & complex schedules (replacing the removed 3D organizer), `History` ledger with PNG month cards & A4 doctor PDFs, `2 profiles free + optional Supporter unlock`, passphrase-encrypted `.havn` backups, and accurate privacy copy aligned with `AndroidManifest.xml`.

---

## Uploading to Google Play

With Fastlane configured (`fastlane supply`), you can upload text metadata across all 87 locales without re-uploading binaries or overwriting graphics:

```bash
fastlane supply \
  --metadata_path fastlane/metadata/android \
  --skip_upload_aab true \
  --skip_upload_apk true \
  --skip_upload_images true \
  --skip_upload_screenshots true \
  --skip_upload_changelogs true
```

*(Note: Play Console requires at least one APK/AAB release uploaded manually before `fastlane supply` can push store listings via API. Note also that older versions of `fastlane supply`'s `create_app_on_managed_play_store` list omit `sq`, `gu`, `kk`, `pa`, `ur`, `fa-AE`, `fa-AF`, `fa-IR`, while Google Play Console UI and directory uploads via `edits.listings.update` accept all 87.)*

---

## Covered Locales (87)

`af`, `am`, `ar`, `az-AZ`, `be`, `bg`, `bn-BD`, `ca`, `cs-CZ`, `da-DK`, `de-DE`, `el-GR`, `en-AU`, `en-CA`, `en-GB`, `en-IN`, `en-SG`, `en-US`, `en-ZA`, `es-419`, `es-ES`, `es-US`, `et`, `eu-ES`, `fa`, `fa-AE`, `fa-AF`, `fa-IR`, `fi-FI`, `fil`, `fr-CA`, `fr-FR`, `gl-ES`, `gu`, `hi-IN`, `hr`, `hu-HU`, `hy-AM`, `id`, `is-IS`, `it-IT`, `iw-IL`, `ja-JP`, `ka-GE`, `kk`, `km-KH`, `kn-IN`, `ko-KR`, `ky-KG`, `lo-LA`, `lt`, `lv`, `mk-MK`, `ml-IN`, `mn-MN`, `mr-IN`, `ms`, `ms-MY`, `my-MM`, `ne-NP`, `nl-NL`, `no-NO`, `pa`, `pl-PL`, `pt-BR`, `pt-PT`, `rm`, `ro`, `ru-RU`, `si-LK`, `sk`, `sl`, `sq`, `sr`, `sv-SE`, `sw`, `ta-IN`, `te-IN`, `th`, `tr-TR`, `uk`, `ur`, `vi`, `zh-CN`, `zh-HK`, `zh-TW`, `zu`
