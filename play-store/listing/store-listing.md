# Hävn — Google Play Store Master Listing & ASO Pack (v2.0.1 · versionCode 31)

- **Package ID:** `com.havnmeds.app`
- **Version:** `2.0.1` (`versionCode = 31`, Target/Compile SDK `36`)
- **Category:** Medical (or Health & Fitness)
- **Tags:** Medication reminder, Pill tracker, Medicine alarm, Health & fitness

---

## 1. App Title Options (Strictly ≤ 30 characters)

1. **`Hävn: Medication & Pill Alarm`** (`29 / 30` chars) — **Active Default (`en-US`)**
   Combines the Scandinavian brand name with the two highest-volume search intents on Google Play (`Medication` + `Pill Alarm`).
2. **`Hävn: Pill Reminder & Tracker`** (`29 / 30` chars)
   Alternative variation targeting users searching for adherence and refill tracking.
3. **`Hävn Medication Reminder`** (`24 / 30` chars)
   Ultra-clean brand + primary category keyword for store experiments.

---

## 2. Short Description Options (Strictly ≤ 80 characters)

1. **`Private offline medicine schedule, refill tracker, doctor PDF & family profiles.`** (`80 / 80` chars) — **Active Default (`en-US`)**
   Maximizes unique high-intent keywords (`offline`, `medicine schedule`, `refill tracker`, `doctor PDF`, `family profiles`) without repeating the words in the title or using Play-prohibited promotional language.
2. **`Calm offline dose alerts, cyclic schedules, adherence ledger & encrypted backup.`** (`80 / 80` chars)
   Alternative variant highlighting cyclic/taper schedules and passphrase-encrypted `.havn` backups.

---

## 3. Full Description (`en-US`, 1,945 / 4,000 characters)

```text
Stay on schedule with every dose, without handing your health data to the cloud. Hävn is a calm, private medication reminder and pill tracker designed around paper-and-ink clarity, reliable offline alarms, and effortless adherence logs for you and your family.

Whether you take one daily vitamin, manage a cyclic prescription, follow a step-down taper, or help a parent keep track of refills, this medication reminder keeps every dose clear at a glance.

WHY CHOOSE HÄVN
• 100% Offline & Local-First: No account, no sign-up, no cloud servers, and zero analytics or behavioral tracking. Your health records stay on your phone.
• Built for Real-World Schedules: Set daily times, specific weekdays, cyclic regimens (such as 21 days on / 7 days off), step tapers, as-needed (PRN) doses, and minimum hourly spacing.
• Actionable Notifications & Quick Settings: Mark a dose "Took it" or snooze "In 15 min" directly from the notification shade, the Android Quick Settings tile, or the home-screen widget.

TODAY VIEW & MEDICINE SHELF
• Clear Today Timeline: Doses are grouped into Morning, Midday, Evening, and Night windows with an interactive DayTape timeline.
• Detailed Medicine Shelf: Record strength, unit (mg, mcg, mL, IU, drops, puffs), form (tablet, capsule, liquid, injection), food notes (with food, before meals, empty stomach), and pill supply counts with low-stock alerts.

HISTORY LEDGER & DOCTOR REPORTS
• 7, 30 & 90-Day Adherence Ledger: Review your streak, on-time completion rate, and daily ink stamps.
• Shareable Summaries: Export a clean monthly PNG card or generate a formatted A4 PDF medication report for your next doctor or pharmacist visit.

FAMILY PROFILES, TRAVEL & BACKUPS
• Multiple Profiles: Manage up to 2 profiles on one device at no cost.
• Smart Travel Time-Zone Rules: Choose whether doses follow your home clock or adapt to local time when traveling.
• Encrypted Backups: Export and restore passphrase-encrypted .havn backup files stored entirely under your control.

TRANSPARENT ACCESS
All core medication reminder, scheduling, history, PDF export, and encrypted backup features work with no subscription and a distraction-free interface. An optional one-time Supporter upgrade unlocks unlimited family profiles and supports independent development.

Hävn is an organizational tool and does not provide medical advice. Always follow your physician's or pharmacist's instructions.
```

---

## 4. What's New (`versionCode 31` · `2.0.1`)

```text
• Fixed a crash on the History screen when rendering the kept-days figure card and across month-end date transitions.
• Hardened Monthly Card PNG export and Doctor Summary PDF sharing against background layout and share-target errors.
• Performance, stability, and typography refinements across phones, foldables, and tablets.
```

---

## 5. Multi-Device Graphics & 87-Locale Metadata

- **App Icon (512×512):** [`play-store/graphics/icon-512.png`](../graphics/icon-512.png)
- **Feature Graphic (1024×500):** [`play-store/graphics/feature-graphic-1024x500.png`](../graphics/feature-graphic-1024x500.png)
- **Phone Screenshots (1080×1920, 6 scenes):** [`play-store/graphics/screenshots/phone/`](../graphics/screenshots/phone/) (also mirrored in [`play-store/graphics/screenshots/`](../graphics/screenshots/))
- **Flip Phone Screenshots (1080×2640, 4 scenes):** [`play-store/graphics/screenshots/flip/`](../graphics/screenshots/flip/)
- **Foldable Inner-Display Screenshots (1812×2176, 4 scenes):** [`play-store/graphics/screenshots/foldable/`](../graphics/screenshots/foldable/)
- **7-Inch Tablet Screenshots (1200×1920, 5 scenes):** [`play-store/graphics/screenshots/tablet-7/`](../graphics/screenshots/tablet-7/)
- **10-Inch Tablet Screenshots (2560×1600, 5 scenes):** [`play-store/graphics/screenshots/tablet-10/`](../graphics/screenshots/tablet-10/)
- **87-Locale Play Console Translations:** [`fastlane/metadata/android/`](../../fastlane/metadata/android/) and [`play-store/listing/localization-review.md`](localization-review.md)
