# Hävn — Swiss redesign: tasks

Source of truth: `design/swiss/HANDOFF.md` (severe Swiss / Braun). It **replaces**
the earlier "Cinematheque" direction (`design/archive/cinematheque/havn-medication-reminder.html`,
glass capsule, olive, Hanken). Where they disagree, HANDOFF.md wins.

Paste the block below into Cursor / Claude Code as one prompt.

```text
You're working in the Hävn Android repo (Kotlin, Jetpack Compose, Room, Hilt, Glance; package com.havn.app; code in app/src/main). Implement the redesign in design/swiss/HANDOFF.md exactly. Read it first. It replaces the older design/archive/cinematheque/havn-medication-reminder.html direction (glass capsule, olive, Hanken Grotesk); ignore that direction where the two conflict. AGENTS.md and GEMINI.md are stale; update them at the end to point at design/swiss/.

DIRECTION
Severe Swiss / Braun-functionalist. Ink plus one signal colour (#C23A05 light, #FF5A1F dark). Archivo plus IBM Plex Mono. 0 dp corners, no cards, no elevation, 2 dp ink rules and 1 dp hairlines, flush left, nothing between 17 sp and 34 sp, one hero per screen at 4–5× everything else. Motion snaps: Decisive (0.2,0,0,1), no springs, no bounce.

WHAT'S WRONG NOW (fix all of it)
- Launch: stock Android 12 splash showing a crude second copy of the logo (res/drawable/ic_splash_logo.xml) on #FFFFFF, held up to 1500 ms, no exit animation, a white box in dark mode. Then a blank canvas, a 380 ms fade and a shimmer skeleton for local data. themePref defaults to LIGHT (UserPreferences.kt:53, MainActivity.kt:90), so dark phones flash from a dark splash to a white app. enableEdgeToEdge runs late (HavnTheme.kt:87-101).
- Logo: 4 disagreeing copies of a clip-art pill box with an "H" (ic_launcher_foreground.xml, ic_splash_logo.xml, the HavnBrandLogo.kt canvas, play-store icon-512.png), plus a serif "HÄVN" and the tagline "REMEMBER. LIVE BETTER."
- UI-kit softness: 999 dp pills, 22–28 dp radii, glow shadows, spring press-scales, haptic on every tap, aurora blobs (HavnAmbient.kt), breathing circles, shimmer, pulse dots, bounce-and-ripple celebrate, 22 kHz marimba, serif headlines, eyebrows everywhere, filled/outline Material tab icons, fintech area chart, red/amber warnings, and a coffee card plus an agency ad in Settings.
- Today is a dashboard: greeting, toy pill box, odometer, bar, week strip, period groups, rows. Nothing leads.
- Bug: AddMedicationViewModel.kt:147-158 never saves weeklyDays or startDate, so Weekly meds never remind.
- Time picker is a 12 h dial (HavnControls.kt:174), wrong for 24 h users.

TASKS (in order; each step builds and keeps the app working; commit per step)

1. FONTS AND TOKENS
- Add Archivo (variable, with wght and wdth axes if available) and IBM Plex Mono (Regular, Medium) to res/font from Google Fonts (OFL). Delete instrument_serif*.ttf and hanken_grotesk.ttf once nothing references them.
- Archivo's figures may be proportional, so set fontFeatureSettings = "tnum" on every numeral style. Verify with a test that "0000" and "1111" render the same width.
- Rewrite ui/theme/HavnColors.kt + HavnTokens.kt to the HANDOFF tokens only:
  - paper, surfaceRaised, ink, ink2, ink3, rule, signal, taken (= ink), for light and dark.
  - Map every existing semantic slot onto them: accent → signal; positive and taken → ink; warning and danger → ink with words, never a second hue.
  - Delete Sage/Clay/Amber/Slate/Sand as UI colours; medication colour tags become ink shades or are dropped.
- Contrast: check signal #C23A05 on paper #F2F1EC is 4.76:1 and paper text on a signal bar is 4.76:1, both passing AA. Dark #FF5A1F on #121210 is 6.0:1. ink3 #8E8F85 on paper is only 2.9:1, so use it for non-text or ≥24 sp only, never for labels.
- Dark mode: use font grade (not weight) on Archivo if the axis exists to offset heavier-looking text.
- HavnTypography.kt: hero 96/700 tnum −0.055em; title 34/700 −0.035em; row 17/600; body 15/400; label = Plex Mono 11 caps +0.13em; nav/caption = Plex Mono 9 caps +0.10em. Nothing between 17 and 34.
- Shapes: every radius 0. Spacing tokens 4/8/12/20/32/56, gutter 20 dp. Remove all shadows and tonal elevation.

2. MOTION AND HAPTICS
- Replace ui/theme/HavnMotion.kt with HANDOFF §6: Motion.Decisive, Motion.Exit, State 140, Nav 220, Sheet 260, Stagger 28, CellWave 12, plus rememberReducedMotion().
- Delete press(), settle(), glide() and celebrate() and every spring. Press feedback is the 0.975 scale over 80 ms only on the dose checkbox; elsewhere a plain colour change.
- Haptics: HapticFeedbackType.Confirm on committing actions only (log dose, save, skip). Check the Compose Foundation version (1.7+) and use LongPress below it. Remove every other vibrate() in HavnPress.kt.
- Sound: off by default (UserPreferences.kt:56); remove playSoftChime on save.

3. LAUNCH AND LOGO (the "joke" loading)
- values/colors.xml: splash_background, window_background and ic_launcher_background = paper #F2F1EC. values-night: #121210.
- Splash: replace ic_splash_logo.xml with a fully transparent vector, set windowSplashScreenIconBackgroundColor = splash_background, and give it light status/nav icons (values-v27 for nav). The splash is just paper, then the first frame.
- MainActivity.kt: call enableEdgeToEdge() before setContent. Hold only until theme + session resolve, capped at 600 ms. Add setOnExitAnimationListener with a 140 ms alpha fade on Decisive, no zoom.
- Default themePref to "SYSTEM"; call window.setBackgroundDrawable(ColorDrawable(paper)) when the theme flips.
- Move alarmScheduler.rebuildAlarms() (HavnApplication.kt:49) off cold start.
- Fix every ViewModel that starts with isLoading = true (Home, Progress, Organizer, Reminders, ManageMeds, Settings, Onboarding) so a cold start never shows a skeleton. Show loading only after 300 ms.
- New mark: a typographic wordmark, "Hävn" in Archivo 700 tight tracking, plus a minimal monogram for the launcher (a solid ink square with a signal 2 dp rule, or a bold "H" set in Archivo; no pills, no box drawing).
  - Rebuild ic_launcher_foreground.xml and ic_launcher_monochrome.xml from it.
  - Add a white ic_stat_havn.xml for notifications.
  - Re-export play-store/graphics/icon-512.png.
  - Replace HavnBrandLogo.kt with HavnWordmark(); delete the tagline.
- strings.xml: app_name = "Hävn". The Play title stays "Hävn: Medication Reminder".

4. COMPONENTS (ui/components)
- HavnButton: 0 radius. Primary is an ink fill with paper text; secondary is a 1 dp ink outline. No shadow, no scale.
- HavnSurface: delete cards; sections are separated by a 2 dp ink rule or a 1 dp hairline.
- HavnTextField: replace with a SpecRow (mono key above, large value below, hairline under; tap opens a bottom sheet with the input). No floating labels.
- DoseCheckbox: implement HANDOFF §6.2 exactly (20 dp square, ink wipes up, check at >0.65, StrokeCap.Square, Confirm haptic).
- Reveal: HANDOFF §6.1. Use a plain Column for dose rows, and hoist the "shown" flag into the ViewModel so nav-back doesn't replay it.
- MatrixCell: HANDOFF §6.3, diagonal wave.
- Countdown: HANDOFF §6.4, tnum.
- Delete HavnAmbient, HavnStates' shimmer/pulse/breathing, HavnDoseCheck's bounce/ripple, the odometer and the segmented thumb. The segmented control becomes mono text options with a 2 dp signal tick.
- Empty states: one flush-left 34 sp sentence plus one action, no illustration.
- Icons: none in nav. Where an icon is unavoidable (close, back), use a 1.5 dp square-cap line icon in ink.

5. NAVIGATION
- Bottom nav: four mono words (TODAY · HISTORY · MEDS · SETTINGS), 9 sp caps, with a 2 dp signal tick over the active one. No icons, no dot, no spring.
- Tab switch is a 100 ms content crossfade, no slide. Pushed screens use the quarter-width slide + fade on Motion.Nav (HANDOFF §6.5).
- Entrance animations run on app open only, never on nav-back.
- Add android:enableOnBackInvokedCallback="true", and BackHandler in multi-step flows.

6. SCREENS
- TODAY (HomeScreen.kt):
  - Hero is the time until the next dose ("42 min", 96 sp, countdown). It shows elapsed or "Due now" rather than a precise countdown that can finish early.
  - Under it, dose rows split by hairlines: mono time · name + dose · DoseCheckbox.
  - The next dose is a full-bleed signal bar, the only colour on screen. It CUTS to the next row when logged, with no animation.
  - Remove greeting, wordmark row, bell, avatar, odometer, progress bar, week strip, period grouping, organiser box, ambient blobs, DoseDetailSheet and DayCompleteOverlay celebrate. When everything is taken, the hero reads "Done." in ink.
  - Rows still need a visible way to skip or snooze, not a hidden swipe: a long-press sheet plus a visible "···" or text affordance (HANDOFF §8).
- HISTORY (ProgressScreen.kt):
  - Hero is adherence % at 96 sp.
  - Below it, a 7 × 9 week matrix: taken = solid ink, partial = ink 35 %, missed = signal OUTLINE.
  - Column headers in mono (M T W T F S S); 9 weeks so the nav stays in frame. MatrixCell diagonal reveal on first open only.
  - Delete the area chart, calendar pane, segmented control and MiniStats.
- ADD / EDIT (AddMedicationScreen.kt):
  - A spec sheet of SpecRows: NAME, DOSE, FORM, TIMES, REPEAT, START. Each opens a sheet (Motion.Sheet).
  - The TIMES sheet is a 24 h-aware large tabular "08:00" entry (DateFormat.is24HourFormat), replacing the 12 h dial.
  - REPEAT Weekly has a Mon–Sun picker that actually saves weeklyDays, fixing the bug at AddMedicationViewModel.kt:147-158. Also keep startDate and isActive on edit.
- MEDS (ManageMedicationsScreen.kt + OrganizerScreen.kt): fold the organiser box away. MEDS lists medications as spec rows (name / dose / times). Pause is a mono toggle; delete lives only inside edit, written as a word in ink.
- SETTINGS:
  - Mono key / value rows.
  - Theme options LIGHT / DARK / SYSTEM as mono text with a signal tick.
  - Remove the coffee card and the agency ad. Profiles collapse to one row.
- REMINDERS: mono time rows, toggles as square ink switches (0 radius), and "NUDGE AFTER — 10 MIN".

7. ONBOARDING (build LAST, HANDOFF §7)
- Four screens, no carousel:
  1. Name: one skippable field, big type.
  2. First medication: the real Add form.
  3. Their medication rendered in the real Today screen, nothing asked.
  4. Reminders: a yes that triggers the POST_NOTIFICATIONS dialog.
- Plus one full screen with only "No account. No network." Persist onboarding_complete in DataStore.
- Delete the old IdentityPage, aurora, logo and slide transitions.

8. OUTSIDE THE APP
- Notifications:
  - Title "08:00 · Metformin · 500 mg" (mono-style plain text); body is the notes or "Due now".
  - Small icon ic_stat_havn; actions "Taken" / "In 15 min".
  - VISIBILITY_PRIVATE with a public version reading "A dose is due".
  - Make MainActivity singleTop and deep-link the notification tap to that dose.
- Widget (HavnWidget.kt, Glance): paper ground, 0 radius (system radius on API 31+ only for the outer shape), and the next dose as an ink time + name + a signal "TAKEN" button. Delete "Take All", the bubbles, bold text and the logo dot.
- Store: delete the mock screenshots and fake-feature screenshot-builder HTML. Retake real screenshots of Today, History and Add in light and dark. Rewrite store-listing.md in short plain sentences with no "calm/mindful/sanctuary".

9. CHECK
- Extend LaunchSmokeTest: night, sdk 26 and 31, landscape, font scale 2.0. Screenshot Today, History, Add and Onboarding in light and dark.
- Assert:
  - The splash and window colours equal paper.
  - No radius > 0.
  - No colours outside the token set.
  - No serif or Hanken left.
  - Digit widths are equal.
  - No skeleton on cold start.
- Compare the screenshots against the reference sketch https://claude.ai/artifact/DEUdJx7hKUBYM6rxaTb8qJ.
- Update AGENTS.md / GEMINI.md to point at design/swiss/HANDOFF.md.
- None of the Kotlin in HANDOFF.md has been compiled: check it against gradle/libs.versions.toml (Compose BOM, Foundation 1.7 for HapticFeedbackType.Confirm, togetherWith).

DONE MEANS: tapping the icon shows plain paper, then Today with "42 min" huge, rows ruled in ink and one orange bar on the next dose. Nothing is rounded, nothing bounces, and there's one colour.
```
