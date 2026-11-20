# Hävn for iOS: App Store guidebook

How to build Hävn as a free, offline, private iPhone app, and how to get it through App Review in 10 languages.

Checked against Apple's own pages on 18 November 2026. Each rule below says where it comes from. Anything marked **(from memory)** is something I know but did not re-read today: check it before relying on it.

---

## 1. Why the last iOS build was rejected

App Store Connect rejected version 1.0.0 under **guideline 2.5.4**:

> "Multitasking apps may only use background services for their intended purposes"

That build declared a background mode (in `UIBackgroundModes`) that a reminder app doesn't need. The new build declares **none**:

| Need | How Hävn does it | Background mode? |
|---|---|---|
| Dose reminders | Local notifications, scheduled ahead of time (`UNUserNotificationCenter`) | No |
| Rings through Silent | AlarmKit alarms, for the medications the person picks | No |
| Widgets update | WidgetKit timelines | No |
| Sounds | Played only while the app is open | No |

**Rule for the project:** `UIBackgroundModes` stays out of Info.plist. If a feature ever seems to need it, find another way first.

---

## 2. The local, private build

Everything stays on the iPhone. There is no server, no account and nothing to sign in to.

### What the code does

| Area | Decision | Why |
|---|---|---|
| Storage | SwiftData store inside the app's own container. Widgets read from an App Group container that only Hävn's own app and widgets can open. | Data never leaves the device |
| Network | No networking code. No third-party SDKs: no analytics, no crash reporters, no ads. | Keeps the App Privacy answer at "Data Not Collected" (section 4) |
| iCloud | **No iCloud or CloudKit sync.** | Guideline 5.1.3(ii): apps "may not store personal health information in iCloud" |
| iPhone backups | The store is left out of iPhone backups by default. A switch in Settings, *Include in iPhone Backups*, puts it back in. | Same rule, read cautiously. Trade-off: with the switch off, a lost or replaced iPhone loses the history unless the person exported it. **Your call** (section 9) |
| Export | *Export* in Settings writes a file the person saves with the share sheet. They choose where it goes. | The person's own backup, on their terms |
| Erase | *Erase All Data* in Settings, with a confirmation. Deleting the app also removes everything. | The person stays in control |
| File protection | iOS default ("protected until first unlock") | Widgets and alarms still need to read the schedule while the iPhone is locked |
| Lock Screen | Notifications say "Time for your 13:00 dose · 1 medication is due". Names show only if the person turns on *Show Names on Lock Screen*. | People leave phones on desks |
| App lock | Optional *Lock with Face ID* | Shared phones and families |
| Apple Health | **Read only**: imports the medication list so people don't retype it. Hävn never writes to Health. | Smallest possible access; nothing written that could be wrong |
| Sounds | Short in-app sounds, off with the Settings switch, quiet in Silent mode | Matches the sound rules in `design/swiss/VISUAL_CHECKLIST.md` |
| Money | Free, no in-app purchases, no tip link | Guideline 3.1.1: a tip to the developer must use in-app purchase, so the Android "Buy me a coffee" link can't come across |

### Files already in this folder

| File | What it is |
|---|---|
| `Havn/PrivacyInfo.xcprivacy` | The privacy manifest Apple requires. Says: no tracking, no data collected, and gives the reason codes for `UserDefaults` (CA92.1 for the app's own settings, 1C8F.1 for the App Group shared with widgets). Before each release, run Xcode's *Generate Privacy Report* on the archive and add any other "required reason" API it lists. |
| `Havn/Localizable.xcstrings` | Every piece of text in the app, in 10 languages (section 6) |
| `Havn/InfoPlist.xcstrings` | The app's name under the icon and the four permission prompts (Health, AlarmKit, Camera, Face ID), in 10 languages |
| `PRIVACY.md` | The iOS privacy policy, ready to publish |

### Info.plist keys

| Key | Value |
|---|---|
| `NSHealthShareUsageDescription` | From `InfoPlist.xcstrings`. Required to read from Health. |
| `NSAlarmKitUsageDescription` | From `InfoPlist.xcstrings`. Apple: "If the `NSAlarmKitUsageDescription` key is missing or its value is an empty string, apps can't schedule alarms with AlarmKit." Developers have reported that this one string doesn't pick up translations: check the prompt on a device in two languages. |
| `NSCameraUsageDescription` | From `InfoPlist.xcstrings`. Asked the first time someone scans a pharmacy label or photographs a pill. Choosing a photo with `PhotosPicker` needs no permission. |
| `NSFaceIDUsageDescription` | From `InfoPlist.xcstrings` |
| `ITSAppUsesNonExemptEncryption` | `NO`. Hävn uses only the encryption built into iOS, so this skips the export-compliance question on every upload. **(from memory)** |
| `UIBackgroundModes` | **Absent** (section 1) |
| `NSHealthUpdateUsageDescription` | **Absent**: Hävn doesn't write to Health |

---

## 3. What Apple requires before you publish

| Requirement | Source | Status |
|---|---|---|
| Built with Xcode 26 and the iOS 26 SDK | Apple's upload requirement since 28 April 2026 | Needs a Mac or a cloud Mac (section 9) |
| Privacy manifest | Apple, required since May 2024 | Done: `Havn/PrivacyInfo.xcprivacy` |
| Privacy policy link in App Store Connect **and** inside the app | Guideline 5.1.1(i): "All apps must include a link to their privacy policy in the App Store Connect metadata field and within the app in an easily accessible manner." | Text done (`PRIVACY.md`). It needs a live web page: `https://mander.tech/havn/privacy` is a placeholder until the page exists. The app links to it from Settings › About Hävn › Privacy Policy. |
| A purpose string for every permission | Guideline 5.1.1(ii) | Done, in 10 languages |
| No sign-in unless there are account features | Guideline 5.1.1(v) | No account at all |
| No health data in iCloud | Guideline 5.1.3(ii) | No iCloud sync (section 2) |
| Tell people to check with a doctor | Guideline 1.4.1: "Apps should remind users to check with a doctor in addition to using the app and before making medical decisions." | In onboarding, in Settings › About, and at the end of every product page description |
| Name of 30 characters or fewer, accurate metadata | Guideline 2.3 | Checked by `tools/check.py` |
| Keywords that describe the app; no trademarks or other app names | Guideline 2.3.7: "don't try to pack any of your metadata with trademarked terms, popular app names, pricing information, or other irrelevant phrases" | Done. Never add "Apple Health", "Medisafe" or similar. |
| Screenshots and text suitable for ages 4+ | Guideline 2.3.8 | Fine |
| **Never mention Android** in the iOS app or its product page | Guideline 2.3.10: "don't include names, icons, or imagery of other mobile platforms" | Fine. Keep it that way in "What's New" too. |
| Screenshots show the app in use | Guideline 2.3.3 | Take them from the real app, not from the mockups |
| Age rating questionnaire, including the new questions | Apple: ratings are now 4+, 9+, 13+, 16+ and 18+. New questions appeared on 16 November 2026, and answers are required by 12 January 2027. | Answer honestly; Hävn should come out **4+**. Read the medical or treatment content question carefully: Hävn reminds, it doesn't give medical information. |
| EU trader status (Digital Services Act) | Apple: an app without a trader status declaration is removed from the EU App Store | **Your call** (section 9). A trader's address, phone and email are shown on the EU product page. |
| App Privacy answers | Section 4 | "Data Not Collected" |
| Export compliance | Section 2 | `ITSAppUsesNonExemptEncryption = NO` |

---

## 4. App Store Connect answers

### App Privacy

Answer **"No, we do not collect data from this app."** The product page then shows "Data Not Collected".

Apple's definition: "'Collect' refers to transmitting data off the device in a way that allows you and/or your third-party partners to access it for a period longer than what is necessary to service the transmitted request in real time." And: "Data that is processed only on device is not 'collected' and does not need to be disclosed in your answers."

This stays true only while there is no networking code and no third-party SDK. If either is ever added, these answers must be redone first.

### Accessibility Nutrition Labels

App Store Connect lets you declare which accessibility features work across the app's common tasks. Declare a feature only after testing every common task with it (add a medication, log a dose, undo, read History, change a setting):

- VoiceOver
- Voice Control
- Larger Text (test with German at the largest size: it's the longest language)
- Dark Interface
- Differentiate Without Color Alone (the Day Ring uses a check and a count as well as colour)
- Sufficient Contrast (the light-mode accent was deepened to #007A8F for this: 5.02:1)
- Reduced Motion (every animation has a Reduce Motion version)

### Other fields

| Field | Answer |
|---|---|
| Price | Free, every territory |
| In-app purchases | None |
| Primary category | Health & Fitness (where Streaks and Gentler Streak sit). Alternative: Medical. **Your call** |
| Secondary category | Medical |
| Copyright | 2026 Mander |
| Support URL | `https://mander.tech/havn` (placeholder until the page exists) |
| Sign-in for review | Not needed: there is no account |

### Notes for App Review (paste into App Review Information › Notes)

Update the menu paths to match the build before pasting.

```
Hävn is a free medication reminder that works entirely offline.

- No account, no sign-in, no servers, no analytics. All data is stored on the device.
  Nothing is synced to iCloud.
- Reminders are local notifications. The app declares no background modes.
- Alarms (optional, per medication) use AlarmKit so a dose can ring through Silent mode.
  To see one: Medications › any medication › Reminder › Alarm.
- Apple Health is read-only: Settings › Read Medications from Health imports the medication
  list. The app never writes to Health.
- The app reminds people to follow their doctor's or pharmacist's instructions
  (onboarding and Settings › About Hävn).

To try it: complete the short onboarding, add a medication with a time a minute or two
away, then lock the device to see the reminder.
```

---

## 5. Steps to ship, in order

1. **Apple Developer Program.** Enrol as an organisation under Mander (needs a D-U-N-S number) or as an individual. The name enrolled is the seller name on the product page. **(from memory)**
2. **A Mac with Xcode 26**, or a cloud Mac (section 9).
3. **Create the Xcode project** in `ios/`: iOS 26 target, bundle ID such as `tech.mander.havn`, an App Group for the widgets, the HealthKit capability (read only). Add the three files from `ios/Havn/` to the app target.
4. **Build the app** from `design/ios/BRIEF.md` and the motion prototype.
5. **Test in every language.** In Xcode: Product › Scheme › Edit Scheme › Run › App Language. Also run the *Double-Length Pseudolanguage* to find text that gets cut off.
6. **TestFlight.** Internal testers first. External testers need a short Beta App Review. **(from memory)**
7. **App Store Connect record.** New App, iOS, primary language English (U.S.), name "Hävn: Medication Reminder". Add the 13 other localizations (section 7).
8. **Screenshots** from the real app on a 6.9-inch iPhone: 1320 × 2868 pixels, portrait. Apple scales them down for smaller iPhones. Take them in each language (section 7).
9. **Fill in sections 3 and 4**: privacy policy URL, App Privacy, age rating, trader status, accessibility labels, review notes.
10. **Upload the build** from Xcode (Product › Archive › Distribute), pick it in App Store Connect, submit for review.
11. **After approval**, search the App Store for "havn" (no dots on the a). If Hävn doesn't come up, add `havn` to the keywords in the next update.

---

## 6. Languages inside the app

### Languages

| Language | Xcode code | App Store localization(s) |
|---|---|---|
| English (source) | `en` | English (U.S.), English (U.K.) |
| French | `fr` | French, French (Canada) |
| Spanish | `es` | Spanish (Spain), Spanish (Mexico) |
| Portuguese (Brazil) | `pt-BR` | Portuguese (Brazil) |
| Portuguese (Portugal) | `pt-PT` | Portuguese (Portugal) |
| German | `de` | German |
| Italian | `it` | Italian |
| Norwegian (Bokmål) | `nb` | Norwegian (`no` in fastlane) |
| Swedish | `sv` | Swedish |
| Japanese | `ja` | Japanese |
| Korean | `ko` | Korean |

### How it works

- **String Catalogs.** Xcode keeps every string and its translations in `Localizable.xcstrings`. In SwiftUI, `Text("Taken")` and `String(localized: "Taken")` look the key up automatically.
- **One source until the project exists.** For now, `tools/build_localization.py` holds every string and translation and writes both catalogs. Once the Xcode project exists, Xcode owns the catalogs: edit them in Xcode, and send work to translators with Product › Export Localizations (XLIFF files), then Import Localizations.
- **Plurals** are variations in the catalog: "1 medication is due" / "2 medications are due". Japanese and Korean have only one form, and the catalog already reflects that.
- **Placeholders.** Translations can reorder them (`%2$lld件中%1$lld件服用済み` in Japanese). `tools/check.py` fails if a translation drops, adds or changes one.
- **Never build sentences by joining pieces.** Word order differs: Korean puts the time before the medication name.
- **Dates, times and numbers**: always use `FormatStyle` (`.formatted(date: .omitted, time: .shortened)`), so each person gets 13:00 or 1:00 PM as their iPhone is set.
- **Apple's own words.** Before release, set an iPhone to each language and copy what Apple Health uses for Taken, Skipped and "Log All as Taken". Hävn should use the same words.

### Review before release

Every translation is marked **needs review** in the catalogs. A native speaker should read each language in the running app, most of all the medical disclaimer and the permission prompts. Then mark the strings reviewed in Xcode.

---

## 7. Product pages in 14 store locales

### What's ready

Each folder in `fastlane/metadata/` holds one product page: `name`, `subtitle`, `promotional_text`, `description`, `keywords`, `privacy_url` and `support_url`. The folder names are the ones fastlane uses (checked against fastlane's own language list).

Lengths, as `tools/check.py` measured them:

| Locale | Name (30) | Subtitle (30) | Promo (170) | Description (4000) | Keywords (100 bytes) |
|---|---|---|---|---|---|
| en-US | 25 | 29 | 139 | 1202 | 96 |
| en-GB | 25 | 29 | 139 | 1204 | 96 |
| fr-FR | 28 | 26 | 154 | 1406 | 98 |
| fr-CA | 28 | 26 | 154 | 1406 | 98 |
| es-ES | 27 | 26 | 156 | 1359 | 93 |
| es-MX | 27 | 26 | 156 | 1359 | 93 |
| pt-BR | 26 | 28 | 146 | 1254 | 97 |
| pt-PT | 27 | 29 | 141 | 1295 | 94 |
| de-DE | 29 | 26 | 168 | 1409 | 96 |
| it | 24 | 29 | 161 | 1328 | 93 |
| no | 23 | 28 | 147 | 1230 | 86 |
| sv | 23 | 29 | 149 | 1252 | 86 |
| ja | 13 | 17 | 74 | 557 | 93 |
| ko | 11 | 18 | 82 | 601 | 75 |

English (U.S.): **Hävn: Medication Reminder** · *Private, offline pill tracker*

### Rules the text follows

- **Keywords** are counted in bytes, and a Japanese or Korean character takes 3 bytes, so those lists are shorter. Commas only, no spaces. Words already in the name or subtitle are left out: Apple searches all three together.
- **The description lists only what the build does.** It currently lists everything in the design brief (several people, widgets, Health import, supply tracking). Remove any line that isn't in the version you submit (guideline 2.3).
- **Promotional text** sits above the description and can be changed at any time without a new version. **(from memory)**
- **French (Canada) and Spanish (Mexico)** start as copies of French and Spain Spanish. A local speaker can adjust them later.

### Screenshots per language

Screenshots with text should be in the page's language. Run the app in each language (section 5, step 5) and take the same set: Today with the ring half done, the ring closed, History, adding a medication, and the Lock Screen reminder. A locale with no screenshots of its own shows the primary language's. Up to 10 screenshots per locale. **(from memory)**

### Uploading the text

Either paste each field into App Store Connect, or use fastlane from this folder:

```
cd ios
fastlane deliver --skip_binary_upload --skip_screenshots   # text only
```

Run `python3 tools/check.py` first. It exits with an error if any field is over its limit.

---

## 8. Before every release

- [ ] `python3 ios/tools/check.py` passes
- [ ] Xcode's Privacy Report lists only what `PrivacyInfo.xcprivacy` declares
- [ ] Info.plist has no `UIBackgroundModes`
- [ ] No new networking code or third-party package (otherwise redo App Privacy)
- [ ] Privacy policy page is live and matches `PRIVACY.md`
- [ ] The description and screenshots match what's in this build
- [ ] Each language checked in the running app, including the permission prompts
- [ ] "What's New" written in all 14 locales, without mentioning Android

---

## 9. Decisions for you

1. **Mac or cloud Mac.** Xcode 26 runs only on a Mac. Without one, a hosted macOS runner (GitHub Actions, for example) can build and upload, but designing and testing on a simulator is much slower that way.
2. **EU trader status.** If Mander is a business, it's a trader and its contact details appear on the EU product page. Declaring is required either way.
3. **iPhone backups on or off by default.** Off is the safest reading of guideline 5.1.3; on means a new iPhone keeps the history without an export.
4. **Primary category**: Health & Fitness or Medical.
5. **Privacy and support pages**: confirm the URLs, or give me the real ones and I'll update every locale.
6. **What ships in 1.0**: so the description can be trimmed to match.
7. **iPhone only, or iPad too.** iPad needs its own screenshots.
