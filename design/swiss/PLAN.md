# Hävn visual overhaul — 2-week plan

How it works each day:
1. Claude does the day's task and pushes it.
2. You check it (preview link, or build in Android Studio and look on your phone).
3. You reply **approve** or what to change. Nothing moves to the next day until it's approved.

Homework (HW) is what only you can do: pick, buy, shoot, test on a real phone. Each day's HW feeds a later day, so it's listed early.

Reference: `VISUAL_CHECKLIST.md` (what), `IDEAS.md` (approved ideas), `preview.html` / `onboarding.html` (how it should look).

---

## Week 1 — Foundations and the main screen

### Day 1 — Tidy the project
- **Claude:** organise the repo so everything is findable: one `PROJECT_MAP.md` at the root, old Cinematheque design moved to `design/archive/`, Swiss work in `design/swiss/`, stale docs marked. No app code changes.
- **You approve:** you can find any file from `PROJECT_MAP.md` in under a minute.
- **HW:** install/update Android Studio, make sure the current app builds and runs on your phone. Tell me your phone model and Android version.

### Day 2 — Colours, fonts, shapes (no screens yet)
- **Claude:** swap the palette to paper/ink/orange, add Archivo + IBM Plex Mono, set radius 0, remove shadows, new type scale with tabular numbers. Old screens will look half-new; that's expected.
- **You approve:** app builds; colours and fonts look right in light and dark.
- **HW:** check the fonts' licences are fine for you (both are free, SIL Open Font License). Start the photo hunt (see HW list A).

### Day 3 — Motion and feel
- **Claude:** new motion tokens (decisive easing, 140/220/260 ms, no springs), reduce-motion support, haptic map (dose = click, all done = double, undo = light).
- **You approve:** taps feel crisp; nothing bounces; vibrations feel right on your phone.
- **HW:** with your phone, note which vibrations feel too strong/weak.

### Day 4 — Today screen, part 1
- **Claude:** masthead `WED 14 OCT`, giant next-dose time / countdown, dose rows with mono time column, 2 dp rule above "next", orange slab on the next dose only.
- **You approve:** Today matches `preview.html` frame 01/02 on your phone.
- **HW:** use the app for a real day with 2–3 test meds; write down anything that feels off.

### Day 5 — Taking a dose
- **Claude:** the 600 ms moment: ink wipe on touch-down, row settles grey, `TAKEN 08:02` stamp, orange cuts to next row, countdown flips, `UNDO` strip for 4 s.
- **You approve:** taking and undoing a dose feels satisfying and clear.
- **HW:** sound brief — pick from HW list B which direction you like.

### Day 6 — Living day + all done
- **Claude:** "now" line moving down the list, 24 h ruler header, week strip, missed-dose state, "That's everything." with summary and a field note.
- **You approve:** the screen visibly changes through a day; the done screen feels like an ending.
- **HW:** read `FIELD_NOTES.md`, cross out any lines you don't like.

### Day 7 — Buffer + week review
- **Claude:** fix everything you flagged this week; screenshots of every screen in light/dark/200% font.
- **You approve:** week 1 sign-off.
- **HW:** deliver photos (HW list A) and sound picks (HW list B).

---

## Week 2 — Everything else, then polish

### Day 8 — Launch + onboarding
- **Claude:** new launch (`HÄVN` wordmark, no logo circle, no white flash), 5-step onboarding with the backgrounds and word reveals from `onboarding.html`.
- **You approve:** first open on a freshly installed app feels like the preview.
- **HW:** uninstall/reinstall to test first run; say how long it felt.

### Day 9 — Add medication + Meds cabinet
- **Claude:** spec-sheet add/edit screen, big numeral time entry, day squares for repeat; Meds as an index list with a detail page (`22 left` hero).
- **You approve:** adding a med is quick and looks like the spec sheet.
- **HW:** add 5+ real-ish meds to stress-test layouts.

### Day 10 — History
- **Claude:** month poster (`OCTOBER`, full-bleed grid), plain summary sentence, tap a day for exact times, diagonal-wave fill.
- **You approve:** History reads in 3 seconds.
- **HW:** none — rest day for you.

### Day 11 — Settings, About, night mode
- **Claude:** contents-page Settings, square toggles, sounds/haptics switches, colophon About page, designed dark mode + optional "dark after 22:00".
- **You approve:** night mode looks designed, not inverted.
- **HW:** use the app at night once and report.

### Day 12 — Sound, imagery, widget, notifications
- **Claude:** drop in your sounds and photos (duotone, compressed), restyle the widget (poster tile, done/empty states), notification as a headline with Taken / In 15 min / Skip.
- **You approve:** widget and notification look like the app.
- **HW:** add the widget to your home screen for a day.

### Day 13 — Icon, store graphics, organiser decision
- **Claude:** new launcher icon + themed monochrome icon; 6 poster-style store screenshots + feature graphic; 3D organiser either restyled in ink lines or hidden for v2 (your call).
- **You approve:** store page looks like one brand.
- **HW:** decide organiser: keep or park.

### Day 14 — Final pass
- **Claude:** run the final review list (light/dark, 200% font, 0/1/5/15 meds, reduce motion), fix leftovers, remove any old sage/serif traces, bump version.
- **You approve:** ready for closed testing.
- **HW:** build the release `.aab`, upload to closed testing, send the tester email.

---

## Your homework lists

### A. Imagery (due Day 7)
- [ ] 4–6 architectural photos: concrete stairs, window grid, colonnade shadow, bridge underside, a doorway
- [ ] Your own photos, or free-licence (e.g. Unsplash / Pexels — keep the photographer's name for credits)
- [ ] Shoot/find in high resolution, landscape and portrait; avoid people, logos and readable signs
- [ ] Drop them in `design/swiss/assets/photos/` (or send them to me)

### B. Sound (due Day 7)
- [ ] Choose a direction: (1) dry paper/wood tick, (2) soft glass tap, (3) keep the felt marimba but shorter
- [ ] Optional: record your own taps (pencil on desk, closing a notebook) in a quiet room, phone voice memo is fine
- [ ] Free-licence sources if needed (e.g. freesound.org, CC0 filter)
- [ ] Need: dose taken (≤ 0.3 s), all done (≤ 1 s), reminder (≤ 1.5 s)

### C. Lottie (optional, due Day 12)
- [ ] Decide if you want any: launch wordmark, "Course complete." mark, empty-state line
- [ ] If yes: a motion designer or LottieFiles creator, custom-made in ink + orange, 1.5–2 dp strokes, no bounce
- [ ] Skip stock themes

### D. Device and accounts
- [ ] Android Studio working, phone in developer mode, USB debugging on
- [ ] Play Console access for closed testing
- [ ] 3–5 testers' emails ready for Day 14

### E. Decisions I'll ask for (one line each)
- [ ] Day 2: keep or drop per-med icons
- [ ] Day 6: countdown switches at < 60 min, or always show time?
- [ ] Day 11: auto-dark after 22:00 — on by default?
- [ ] Day 13: organiser keep or park
