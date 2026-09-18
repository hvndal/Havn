# Publishing Hävn to Google Play

## Fastest: one command

```bash
bash build-release.sh
```

Installs what's missing, creates your upload key on first run, and drops
`Havn-<version>.aab` into `release-output/`. Runs in GitHub Codespaces
(Code → Codespaces → Create), Linux or macOS.

---

## 1. Create your upload key (once, on your own computer)

```bash
keytool -genkeypair -v -keystore havn-upload.jks -alias havn \
  -keyalg RSA -keysize 2048 -validity 10000
```

Keep `havn-upload.jks` and its passwords safe and backed up. Never commit them.

## 2. Add GitHub secrets

Repo → Settings → Secrets and variables → Actions → New repository secret:

| Secret | Value |
|---|---|
| `HAVN_KEYSTORE_BASE64` | output of `base64 -w0 havn-upload.jks` (macOS: `base64 -i havn-upload.jks`) |
| `HAVN_KEYSTORE_PASSWORD` | keystore password |
| `HAVN_KEY_ALIAS` | `havn` |
| `HAVN_KEY_PASSWORD` | key password |

## 3. Build

Actions → **Release build (Play Store)** → Run workflow.
Download the `havn-release` artifact → `app-release.aab` is what you upload.

(Local alternative: fill in `keystore.properties`, then `./gradlew bundleRelease`.)

## 4. Play Console

1. Create app → name **Hävn Medication Reminder**, Free, App.
2. Enable **Play App Signing** (default) and upload the `.aab` to **Internal testing** first, then Production.
3. New personal developer accounts must run a closed test with testers before Production access — check the Dashboard for the exact requirement.
4. App content:
   - Privacy policy URL: link to `PRIVACY.md` on GitHub (or host it on mander.tech/havn/privacy)
   - Ads: **No**
   - Data safety: **No data collected, no data shared**
   - Health apps declaration: medication/reminder tracking, no data leaves the device
   - Content rating questionnaire → Everyone
   - Target audience: 18+ (avoids Families policy)
5. Every future update: bump `versionCode` in `app/build.gradle.kts`.

## Store listing (copy/paste)

**Short description (80):**
Simple, private pill reminders. No account, no ads, works offline.

**Full description:**
Hävn is a calm, private medication reminder.

• Today view: see every dose, grouped by morning, afternoon, evening and night
• 3D pill organizer you can tap and spin
• Reminders with "Mark taken" and "In 15 min" right from the notification
• Progress: 30-day trend and calendar
• Profiles: several people can share one phone
• Light and dark themes
• Home-screen widget
• Backup to a file you control

Private by design: no account, no servers, no tracking, no internet permission. Your medication history never leaves your phone.

Free with no ads. If Hävn helps you, you can support it with a coffee from inside the app.

Made by Mander — mander.tech — web design, apps and projects.

Hävn is a reminder tool and does not provide medical advice.

**Category:** Medical (or Health & Fitness) · **Tags:** medication reminder, pill tracker
**Graphics needed:** 512×512 icon, 1024×500 feature graphic, 2–8 phone screenshots (light + dark)
