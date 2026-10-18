# Hävn — Visual overhaul checklist

Scope: what the final app looks, moves, sounds and feels like. Not code, not features.
Direction: Swiss editorial. Paper, ink, one orange. Big type is the hero. Every visual carries information.

**Legend**
- [x] decided and drawn in the previews (`preview.html`, `onboarding.html`)
- [~] exists in the current app, but in the old look (sage, linen, serif) and needs redoing
- [ ] still to design

Current app, for reference: sage/linen palette, Hanken Grotesk + Instrument Serif, soft springs, card shadows, felt-marimba sounds, haptics on a few controls, 3D organiser view.

---

## 1. Foundations

### 1.1 Colour
- [x] Light: paper `#F2F1EC`, raised `#FBFAF7`, ink `#14140E`, ink-2 `#5A5B50`, ink-3 `#8E8F85`, rule `#D3D2C9`, signal `#C23A05`
- [x] Dark: paper `#121210`, raised `#1A1A17`, ink `#EDECE4`, ink-2 `#9C9D93`, ink-3 `#6A6B62`, rule `#2A2A26`, signal `#FF5A1F`
- [~] Replace the sage/linen/forest palette everywhere, including widget and notification accent
- [ ] Second paper tone for "material character" (one step darker than paper) for recessed areas, never a gradient
- [ ] One orange element on screen at a time, always the next thing to do
- [ ] Missed = orange outline, never red fill. Taken = solid ink. Skipped = ink-3 hatch or dash
- [ ] Contrast pass: every text pair ≥ 4.5:1; ink-3 only for non-text (rules, ticks, empty cells)
- [ ] No dynamic colour (Material You) on app screens; the palette is the brand

### 1.2 Typography
- [x] Archivo (display + UI) and IBM Plex Mono (labels, times, metadata)
- [~] Remove Hanken Grotesk and Instrument Serif from the app
- [ ] Type scale, fixed: Poster 96–120 / Display 64 / Title 34 / Body 17 / Label (mono) 11–12 caps, tracked +8%
- [ ] **One giant number or phrase per screen** (the "big type" moment):
  - Today: next dose time `13:00` or countdown `42 min`
  - All done: `That's everything.`
  - History: `94%`, month name as a poster (`OCTOBER`)
  - Med detail: `22 left`
  - Onboarding: one huge line per step
- [ ] Tabular numerals on every time, count, dose and percentage
- [ ] Tight display tracking (−4 to −5%), line height 0.9 for display, 1.4 for body
- [ ] Real punctuation: ’ “ ” – ·, thin space in `65 mg`, dates as `WED 14.10`
- [ ] Mono labels always uppercase via style, never typed in caps
- [ ] Font scale 200%: giant numbers shrink to fit, nothing truncates, nothing overlaps

### 1.3 Grid, rules, shape
- [x] Radius 0 everywhere
- [x] 2 dp ink rule = section start / what's next; 1 dp hairline = between items
- [ ] 4-column grid, 20 dp outer margin, 8 dp baseline
- [~] Remove all card shadows and elevation (currently on surfaces, buttons, segmented control, organiser)
- [ ] Past rows lose their rule; the rule above "next" is the heavy one
- [ ] Checkbox: square 20 dp, 1.5 dp stroke, fills with ink when taken

### 1.4 Iconography and pictograms
- [ ] Icon set drawn on the same 1.5 dp stroke, square caps, no rounded ends
- [ ] Arrows are typographic (→ ←), not icon-font chevrons
- [~] Replace current `MedIcon` set with the new stroke style (or drop per-med icons; pill shapes are parked)
- [ ] Adaptive launcher icon: wordmark-free "H" or "Ä" mark, ink on paper, plus **monochrome themed icon**
- [ ] Notification small icon: same mark, single colour

---

## 2. Motion

### 2.1 Rules
- [x] Decisive easing `cubic-bezier(0.2, 0, 0, 1)`; State 140 ms, Nav 220 ms, Sheet 260 ms, Stagger 28 ms, cell wave 12 ms
- [x] No springs, no bounce, no overshoot
- [~] Replace current spring `press()` / `settle()` and Enter/Exit easings
- [ ] Reduce motion on: reveals become simple fades, backgrounds and drifts stop, countdown still updates
- [ ] Things move only because time moved or the user acted

### 2.2 Signature moves
- [x] Word-mask reveal for big headlines (each word rises from behind its own line)
- [x] Underline / rule draws left → right
- [x] Typed caret for values in onboarding
- [ ] Shared-element slide: tapping a dose row expands it into its detail sheet; the time numeral travels
- [ ] Departures-board flip when the next dose time changes (per digit, 140 ms cuts)
- [ ] "Now" line drifts down Today in real time
- [ ] Course bar fills toward the end date
- [ ] History grid fills in a diagonal wave on open
- [ ] Screen-to-screen: horizontal cut with 8–16 dp offset + fade, 220 ms; never a full slide

### 2.3 Taking a dose (the most important 600 ms)
- [x] Checkbox ink wipe (drawn in onboarding step 4)
- [ ] Touch-down starts the wipe (no waiting for release)
- [ ] Row text settles to ink-3 over 140 ms; the time gets a `TAKEN 08:02` stamp
- [ ] Orange bar cuts to the next dose row
- [ ] Countdown hero re-sets to the new next time with a flip
- [ ] Undo strip slides up: `TAKEN 08:02 · UNDO`, 4 s, then cuts away
- [~] Haptic (exists on dose check) retuned: one crisp click, nothing on scroll

---

## 3. Screens

### 3.1 Launch
- [~] Replace default SplashScreen logo-in-circle
- [ ] `HÄVN` in Archivo on paper, no icon circle; cuts to Today in ≤ 400 ms
- [ ] Optional: one architectural duotone photo on cold start only, once a day max
- [ ] Dark launch at night matches the dark theme, no white flash

### 3.2 Onboarding (5 steps)
- [x] 1 Name — 24 h ruler behind, word-mask headline, typed name
- [x] 2 Privacy — dark, grid fading out, "No account. No network."
- [x] 3 First med — outlined `08:00` drifting, fields reveal, time types in
- [x] 4 It renders — their Today builds, no background
- [x] 5 Reminders — History matrix wave, "Want a nudge?"
- [~] Replace the current onboarding visuals and copy
- [ ] Progress as 5 dashes, orange when passed
- [ ] Skip is a quiet mono text link, top right

### 3.3 Today
- [x] Big next-dose hero, 2 dp rule, dose rows with mono time column
- [ ] Newspaper masthead: `WED 14 OCT` + dateline `3 DOSES · 1 LEFT`
- [ ] Giant countdown or next time (switch at < 60 min to countdown)
- [ ] "Now" line across the list
- [ ] 24 h ruler header with a tick per dose, ticks fill when taken
- [ ] Week strip: 7 small squares, today outlined
- [ ] Orange slab behind the next dose only
- [ ] Missed row: orange outline box, `MISSED 13:00`, actions Take now / Skip
- [ ] Pills left line under each med: `22 LEFT · REFILL ~ 3 NOV`
- [ ] Course bar for meds with an end date
- [ ] Empty state: huge `EMPTY.` + add button, nothing else

### 3.4 All done (completion)
- [x] "That's everything." + `NOTHING UNTIL THU 08:00` + field note
- [ ] Restrained summary: `5 OF 5 · LAST AT 22:04`
- [ ] Clear visual change: list compresses to ink-3, hero takes the space
- [ ] At night: full dark screen, orange only on the field-note label
- [ ] Optional: duotone architectural photo panel (rare, not daily)
- [ ] Never a score, never a streak, never "great job"

### 3.5 Course complete
- [ ] Full-screen "Course complete." poster
- [ ] The course's grid below, then `DELETE IRON?` / `KEEP IN HISTORY`

### 3.6 Add / edit medication
- [x] Spec-sheet layout (preview frame 06)
- [ ] Each field a row with mono label + large value
- [ ] Time picker as big tabular numerals you drag or type, not the Material clock
- [ ] Repeat as day squares `M T W T F S S`
- [ ] Optional rows: end date, pill count, notes
- [ ] Save button full-width ink bar with →

### 3.7 Meds (cabinet)
- [ ] Index-style list: number, name, dose, times, since-date
- [ ] Tap → detail page with `22 left` as the hero, history strip, notes
- [~] Replace current Manage Medications screen styling

### 3.8 History
- [x] 7 × 9 matrix (preview frame 05)
- [ ] Month as a poster: `OCTOBER` huge, full-bleed grid
- [ ] Plain summary sentence above: "Missed 2 this month. Both were 13:00."
- [ ] Tap a day → times taken `08:02 · 13:41 · 19:05`
- [ ] Monthly poster card (shareable pattern, no med names)
- [~] Replace current Progress screen styling

### 3.9 Settings
- [ ] Contents-page layout: `01 REMINDERS`, `02 DATA`, `03 APPEARANCE`, `04 ABOUT`
- [ ] Theme: System / Light / Dark / "Dark after 22:00"
- [ ] Sounds on/off, haptics on/off as square toggles (no pill switches)
- [ ] About page set like a colophon (typefaces, version, "No account. No network. Still.")

### 3.10 3D organiser
- [~] Exists (three.js). Decide: redesign as the transparent capsule with day shelves in ink lines, or retire for v2
- [ ] If kept: line-drawn, no gloss, no shadows, matches paper/ink

---

## 4. Night atmosphere
- [x] Lamp-lit dark palette (preview frame 03)
- [ ] Secondary text one step softer than day
- [ ] Orange is the only light source
- [ ] Giant numbers slightly lighter weight in dark (optical compensation)
- [ ] Optional auto-dark after 22:00 regardless of system

## 5. Imagery (architectural direction)
- [ ] 4–6 photos max, bundled in the app: concrete stairs, a window grid, a colonnade shadow, a bridge underside, a single doorway
- [ ] Treatment: duotone (ink on paper; in dark, paper on ink), high contrast, hard crops
- [ ] Used only at: launch (optional), completion panel (rare), About page, store graphics
- [ ] Never behind text that must be read; never on Today's list
- [ ] Sources: own photos or properly licensed; keep credits on the About page
- [ ] Compressed WebP, under ~150 KB each

## 6. Lottie / animation assets
Recommendation: build almost all motion natively in Compose; it matches the grid exactly and respects reduce-motion. Use Lottie only for small, self-contained moments, and only custom-made in the house style (not stock LottieFiles themes, which would look generic).
- [ ] Optional Lottie 1: launch wordmark `HÄVN` drawing in (≤ 600 ms)
- [ ] Optional Lottie 2: "Course complete." line-drawn seal or rule animation
- [ ] Optional Lottie 3: empty-state pen line under `EMPTY.`
- [ ] Every Lottie: ink + orange only, 1.5–2 dp strokes, no easing bounce, static last frame for reduce-motion, theme-coloured at runtime
- [ ] Not Lottie: dose check, countdown, flip, now line, grid wave (all native)

## 7. Sound
- [~] Current: felt-marimba set (`sfx_lid`, `sfx_take`, `sfx_tap`, `havn_nudge`). Warm, but softer than the new look
- [ ] New set, dry and precise: a short paper/wood "tick" for taking a dose, a two-note low chime for all done, silence for navigation
- [ ] Reminder sound: one clear, non-alarming tone, ≤ 1.5 s, distinct from system sounds
- [ ] All sounds follow touch-sound volume and silent mode; off switch in Settings
- [ ] No sound on scroll, tabs or typing

## 8. Haptics
- [~] Exists on dose check, press, buttons, segmented control, scaffold
- [ ] Map: dose taken = crisp click; all done = soft double; undo = single light; nothing else
- [ ] Respect the system haptics setting

## 9. System surfaces
- [ ] Notification as a headline: title `13:00 IRON`, body `65 mg`, actions Taken / In 15 min / Skip
- [ ] Lock screen layout matches the mono style
- [~] Widget exists (Glance): restyle as poster tile — 2×2 next time huge + med name in mono; 4×2 adds today's row of squares
- [ ] Widget states: next dose / all done "That's everything." / empty
- [ ] Widget dark and light both designed
- [ ] Themed monochrome icon, edge-to-edge, predictive back animation that matches the cut transition

## 10. Copy and voice
- [x] Field notes, 55 lines (`FIELD_NOTES.md`)
- [x] Tone: plain, factual, quietly on your side; no exclamation marks, streaks or confetti
- [ ] Final strings for every state: empty, first day, missed, skipped, paused, course end, all done, error
- [ ] Accessibility labels in the same voice: "Iron, 65 milligrams, 13:00, not taken"

## 11. Store presence
- [ ] Screenshots as posters: one huge line + cropped real UI, 6 frames
- [ ] Feature graphic in the same style
- [ ] 15-second preview video: onboarding motion + taking a dose
- [ ] Icon 512 matches the new mark

## 12. Final review pass
- [ ] Every screen in light, dark, and 200% font
- [ ] Every screen with 0, 1, 5 and 15 meds
- [ ] Reduce motion on: still understandable
- [ ] Screenshots side by side: one orange per screen, one giant element per screen
- [ ] Nothing in the old sage/serif style left anywhere
