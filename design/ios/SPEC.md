# Hävn for iOS: design handoff spec

Everything a SwiftUI developer needs to build what the prototype shows. The prototype is the reference for timing and feel: `design/ios/prototype/havn.html` (open it in a browser, or the published copy).

**Rule zero:** use the system component whenever one exists. Tab bar, sheets, menus, switches, navigation, alerts and the tab lens are drawn by iOS 26 for free, with Liquid Glass, Dynamic Type and accessibility built in. Build only what iOS doesn't have: the Day Ring, pill shapes, the bar above the tab bar's content, widgets and the onboarding hero.

---

## 1. Colour

System colours first. Hex values are what the prototype uses, for checking.

| Role | SwiftUI | Light | Dark |
|---|---|---|---|
| Screen background | `Color(.systemGroupedBackground)` | #F2F2F7 | #000000 |
| Cards, rows | `Color(.secondarySystemGroupedBackground)` | #FFFFFF | #1C1C1E |
| Text | `.primary` | #000000 | #FFFFFF |
| Secondary text | `.secondary` | rgba(60,60,67,.6) | rgba(235,235,245,.6) |
| Separators | `Color(.separator)` | | |
| **Accent** (app tint) | Asset colour `AccentColor` | **#007A8F** | **#40C8E0** |
| Text on accent | Asset colour | #FFFFFF | #000000 |
| Done / taken | `.green` | #34C759 | #30D158 |
| Overdue (graphic) | `.orange` | #FF9500 | #FF9F0A |
| Overdue (text) | Asset colour `OverdueText` | #C93400 | #FF9F0A |
| Destructive | `.red` | #FF3B30 | #FF453A |

The accent was darkened from the usual teal so text on light backgrounds passes 4.5:1 (#007A8F on white is 5.02:1). In dark mode, buttons put black text on the light teal (10.56:1).

**Ring colours** are each medication's own colour, chosen when it's added: indigo, pink, brown, purple, blue, red, yellow, gray (the system colours of the same names). Pill materials (`white, cream, yellow, amber, orange, red, pink, purple, blue, green, brown, gray`) are fixed values that don't change with dark mode, because a white tablet is white in any light. See the `--pc-*` values in the prototype.

## 2. Type

Only Dynamic Type text styles: no fixed sizes. SF Pro for text; SF Pro Rounded (`.fontDesign(.rounded)`) only for numbers in the ring summary, the strength field and widgets.

| Use | Style | Weight |
|---|---|---|
| Screen titles (Today, History…) | `.largeTitle` | bold |
| Onboarding titles | `.title` | bold |
| Row titles | `.body` | semibold |
| Row details, times | `.subheadline` | regular, `.secondary` |
| Section headers | `.subheadline` | semibold, `.secondary` |
| "2 of 4 taken" | `.title2` | semibold, rounded, `.contentTransition(.numericText())` |
| Footnotes under cards | `.footnote` | `.secondary` |

At the accessibility sizes, the ring summary stacks vertically (ring above text) and rows let their text wrap. Check this with `ViewThatFits` or `@Environment(\.dynamicTypeSize).isAccessibilitySize`.

## 3. Layout

- Lists are `.insetGrouped`. Cards have a corner radius of 26. Rows are at least 64 pt tall, with a 36 pt circular icon, 14 pt of space, then text.
- Screen margins are 16 pt. Section headers sit 22 pt above their card.
- The bar above the tab bar is a `.tabViewBottomAccessory` (iOS 26), 56 pt tall. It holds the next dose and a Taken button, and after logging it shows "Iron logged · Undo" for 4 seconds.
- Toolbar, top right: `+` (Add Medication) and the person button (a menu: people, Add Person, Settings).
- Sheets use the system detents. Add Medication and Settings use `.large`; the dose sheet and day sheet use `.medium`.

## 4. The Day Ring

One piece per dose today, starting at 12 o'clock and running clockwise in time order.

- **Geometry:** the stroke is 14% of the diameter, with round caps. The gap between pieces is both caps plus 9°. When every dose is taken, the gaps close and the caps become square.
- **Colour:** a taken piece takes the medication's colour. Pieces fill in the order doses were taken, so earlier colours never move, and neighbouring colours blend into each other over about 5% of the circle.
- **Untaken pieces:** later doses are the track colour (`.quaternary` fill), the next dose is a 38% accent tint, and an overdue dose is a 50% orange tint. Small dots inside the ring mark the doses still to come.
- **Closed:** a check draws in the centre (`.symbolEffect(.drawOn)` on `checkmark`) and the ring scales to 1.06 and back over 0.9 s.
- **Build:** a `Canvas`, or a `Circle().trim(from:to:)` per piece with an `AngularGradient` for the blend. Animate the trim.
- **Accessibility:** one element, labelled "Today's doses", with value "2 of 4 taken, Iron overdue".

The same ring appears small in History (one per day, 34 pt) and in the widgets.

## 5. Motion

All of it should feel like springs, never linear. Every row is in the prototype's catalogue with a Play button.

| Moment | Duration and curve in the prototype | SwiftUI |
|---|---|---|
| Today opens (slow reveal) | each item rises 22 pt and unblurs from 8 pt; 0.85 s, `cubic-bezier(.2,.8,.2,1)`, 0.1 s apart | `.transition(.opacity.combined(with: .offset(y: 22)))` plus a blur modifier, `.smooth(duration: 0.85)`, `.delay(Double(i) * 0.1)` |
| Ring draws in on open | 1.5 s, starting after 0.3 s | `.spring(duration: 1.5, bounce: 0.15).delay(0.3)` on the trim |
| A dose is logged | circle fills with a spring (0.38 s), check draws on (0.32 s, after 0.12 s), ring grows (0.9 s), count rolls | `.spring(duration: 0.38, bounce: 0.35)`, `.symbolEffect(.drawOn)`, `.spring(duration: 0.9, bounce: 0.2)`, `.numericText()` |
| Words change | old text blurs out (0.28 s), new text blurs in (0.36 s) | `.contentTransition(.interpolate)` or `.transition(.blurReplace)` |
| Row → dose sheet | the row grows into the sheet, 0.56 s | `.matchedTransitionSource` + `.navigationTransition(.zoom)` |
| Ring closes | check draws in, ring breathes to 1.06, chord, success haptic | as above |
| Onboarding Welcome | four pieces fly in from the corners (0.9 s each, 0.11 s apart) while the ring turns −30° → 0° over 1.5 s | `.spring(duration: 0.9, bounce: 0.2)` with a delay per piece |
| Onboarding → Today | the hero ring flies into Today's ring (0.82 s) while the rest fades | `.matchedGeometryEffect(id: "ring", in: ns)` |
| Add Medication steps | each step pushes in from the right, 0.46 s | a `NavigationStack` inside the sheet |
| Calendar month change | the month slides 40% and fades, 0.38 s | `.transition(.push(from: .trailing))` |
| Medication detail | system push | `NavigationLink` |
| People menu | grows out of the person button | system `Menu` (iOS 26 does the glass morph) |
| Tab lens, switches, sheets, alerts | system | system |

**Reduce Motion:** every movement becomes a 0.12 s crossfade, and the reveal, fly-ins and breath are skipped. Read `@Environment(\.accessibilityReduceMotion)`.

**No skeletons.** Data is local and loads in milliseconds, so screens appear complete and the reveal does the work. The only real waits are reading Apple Health during onboarding (a small spinner and "Looking in Health…") and making or restoring a backup (a progress ring).

## 6. Liquid Glass

- **Glass:** tab bar, the bar above it, toolbar buttons, sheets, menus, alerts, and widgets in Clear mode. All of these are system components and get glass automatically.
- **Never glass:** medication rows, cards, the ring. Content stays solid; glass floats above it.
- Content scrolls under the bars. Use the system scroll-edge effect, not a solid fade.
- **Reduce Transparency:** the system turns its own glass solid. Anything custom (the onboarding cards, if styled) must check `@Environment(\.accessibilityReduceTransparency)`.

## 7. Haptics and sound

| Event | Haptic | Sound (the "Glass" set, D major) |
|---|---|---|
| Finger down on a dose circle | `.impact(weight: .light)` | small click |
| Dose logged | `.success` | two-note lift, A5 → D6 |
| Ring closed | `.success` | four-note chord + high A |
| Undo | `.impact(weight: .light)` | falling pair |
| Switch on / off | `.selection` | click + soft high note |
| Picking an option | `.selection` | none |
| As-needed limit reached | `.error` | none |
| Erase All Data confirmed | `.warning` | none |
| Reminder notification | system | one gentle phrase under 1.5 s (`reminder`) |

Use `.sensoryFeedback(_:trigger:)`. Sounds play through the ambient audio session: they follow the Silent switch, mix with music, and stay quiet when scrolling, switching tabs or typing. Settings has a Sounds switch. The sound files are synthesised in the prototype; export them as short `.caf` files.

## 8. Screens and states

Every row can be reached in the prototype (scene buttons above the phone, states panel below it).

| Screen | States designed |
|---|---|
| Welcome (5 steps) | first launch · notifications refused · alarm chosen instead · Health import (permission, searching, found list) · restore from a backup (file picker, progress, done) · add manually (lands in an empty Today with Add Medication open) |
| Today | morning · overdue · all done (ring closed) · empty · reminders off (banner) · another person (Mum) · new person (empty) · as-needed logged · as-needed too soon (alert) · daily limit reached · running low · a suggestion (moved, or Not Now) · “Did it help?” waiting, answered |
| Everyone | every person's ring · a person's due doses logged from here · tap through to one person |
| Add Medication (6 steps) | scan the pharmacy label (framing, reading, found, scan again) · filled in from the label (review banner) · linked to your routine · photo of the pill (camera, from Photos, retake, remove) · name required · duplicate name warning · strength required · every day / specific days / every few days / cycle / as needed · several times a day · notification or alarm · ends on a date · shape and colours · review |
| Dose sheet | due · overdue · already taken |
| History | readings (blood sugar, blood pressure, weight; empty; add a reading; out-of-range warning) · the month in rings (recap, share as image) · week of mini rings · calm summary sentence · month calendar (all / some / skipped) · previous month · day sheet · logging a forgotten dose later · first week (empty) · notes on doses and on the day · Report for Your Doctor card |
| Notes | mood, energy and pain from 1 to 5 · Notes card on Today (empty or showing today's note) · writing sheet with Quick Add phrases · note field on the dose sheet (saves as you type) · pencil on rows that have a note · saved, edited, removed |
| Report | options (name, 30 or 90 days, include notes, include as-needed doses, include readings and symptoms) · creating · preview of two or three pages · share (Save to Files, Print, Send a Copy) · no history yet · dark mode (the paper stays white) |
| Medications | Fill Your Pillbox (each medication in turn, not enough left, filled) · routine time and weekend time · I Picked Up a Refill · pharmacy (call, copy number, none yet) · readings next to the medication they're for · list with supply · running low · as needed · archived · edit (archive) · detail with stats · not scheduled today |
| Settings | Share a Schedule (choose person, with or without history and notes) · Daily Routine (weekdays, weekends, per person) · Simple Mode · Read Names Aloud · Focus Filters · readings from Health · people · reminders · follow-up · alarms · names on Lock Screen · sounds and haptics · Health · Face ID lock · iPhone backups · export · restore · erase (confirmation) · privacy policy · about and disclaimer |
| App lock | locked · Face ID success |
| Home Screen widgets | small · medium (Taken button works without opening the app) · large · all done · Clear (glass) look · dark |
| Lock Screen | ring and next-dose widgets · notification with names hidden · held open with Taken / Remind Me in 10 Minutes / Skipped · another person's dose (“Time for Mum’s 12:30 dose”) · “Did it help?” held open with three answers · Live Activity with Taken |
| Simple Mode | next dose with I Took It · just logged · all done · nothing today · leaving (confirmation) |
| Alarm | ringing · name read aloud · Taken · Snooze |
| Apple Watch | face with the Hävn complication · app with the ring · Taken · all done |
| Dynamic Island | Live Activity compact (minutes late or to go) · expanded with Snooze and Taken · logged · Action Button result |
| Control Center and Action Button | Log Next Dose · logged · not due yet (says when, logs nothing) · all done |
| Siri | what's next · log my morning doses · did I take one · a dose that isn't due yet (no Log button) · logged |
| StandBy | day · night (red) · all done |
| Focus | Caring on (only Mum shown, chip in the status bar) · turning it off |
| Accessibility | largest text · Reduce Transparency · Reduce Motion · Increase Contrast |

**Still to design before launch** (no prototype yet): time-zone travel, daylight-saving changes, tapering doses, receiving a shared schedule.

**Prototype shortcuts:** the clock stays at 13:42, so a “Did it help?” check-in shows its real time (14:42) on the Lock Screen straight away; the label, the camera and the Watch are drawn, not live.

## 9. Widgets (WidgetKit)

- **Families:** `.systemSmall`, `.systemMedium`, `.systemLarge`, `.accessoryCircular` (ring with "2/4") and `.accessoryRectangular` ("Dose due now · 1 medication").
- **Taken button:** an App Intent (`Button(intent: LogDoseIntent(id:))`) on the medium and large widgets, shown only for a dose that is due within the hour. After it runs, the timeline reloads and the ring piece animates in with `.contentTransition`.
- Support `widgetRenderingMode` `.accented` and the Clear look: the ring becomes white at two strengths, and text stays white.
- **Privacy:** Lock Screen widgets never show medication names unless the person turns on Show Names on Lock Screen.
- Widgets read the same local store through the App Group (see `ios/APP_STORE_GUIDE.md`, section 2).

## 10. App icon

The layers are in `design/ios/icon/`. Build the icon in Icon Composer (Xcode 26):

- **Background:** `AppIcon-background-light.svg` (white). For dark, `AppIcon-background-dark.svg`.
- **Foreground:** `AppIcon-ring.svg` (the Day Ring in four colours). Icon Composer adds the glass and the edge highlight.
- **Tinted and Clear:** `AppIcon-ring-mono.svg` (one colour at four strengths).

`AppIcon-looks.png` previews all four looks.

## 11. Accessibility checklist

- Every dose circle is a button labelled like "Log Iron, 65 milligrams, overdue since 13:00". After logging, the label becomes "Iron, taken at 13:42. Undo available."
- The ring is a single element with a summary value (section 4).
- Status never relies on colour alone: taken has a check, overdue has the word "Due", and the ring has dots plus the count.
- Every target is at least 44 × 44 pt.
- Keep the contrast pairs from section 1 if colours change.
- Check the four states in the panel under the prototype with each release.

## 12. Notes and the report for your doctor

**Notes**
- **Where:** two kinds of note: one on a dose (the field at the bottom of the dose sheet) and one on a day (the Notes card on Today, and the Note card in a History day).
- **Limits:** plain text, up to 600 characters, stored with the person's other data.
- **Quick Add:** phrases for the common cases: Felt dizzy, Upset stomach, Headache, Slept badly, Feeling good, Forgot, Away from home, Ran out.
- **Today's card:** shows today's note, or "Write a note about today" when there isn't one.
- **Rows:** a row with a dose note shows a small pencil after its time, so notes are findable without opening anything.
- **Privacy:** notes never appear on the Lock Screen or in widgets. They are included in backups, and in a report only if Include Notes is on.

**Report for Your Doctor** (History › Create PDF Report)
- **Options:** name on the report (optional), Last 30 Days or Last 90 Days, Include Notes, Include As-Needed Doses.
- **The period:** ends yesterday, so today's half-finished day never shows as missed.
- **Page 1:**
  - four totals: % taken, doses taken, doses missed, as-needed doses
  - a medications table with a taken bar per medicine
  - day by day: one mini ring per day for 30 days, or one coloured square per day for 90
  - Patterns: at most three plain sentences, such as "Most missed doses were on Saturdays (4 of 7)"
- **Page 2:**
  - missed doses, most recent first, with any dose note (12 shown, then "and N earlier")
  - notes
  - as-needed doses
  - a disclaimer that it's a log, not a medical record
- **Every page:** a footer with the date made and the page number. The paper is always white, A4 in most countries and US Letter in the US, from the region setting.
- **Build:** lay the pages out in SwiftUI and render each one to PDF with `ImageRenderer` into a PDF `CGContext`. Write the file to the temporary folder as "Hävn Report 18 Nov 2026.pdf", hand it to `ShareLink`, and delete it when the share sheet closes. Nothing is kept or uploaded.
- **Samples:** `design/ios/report/Havn-report-sample.pdf` (30 days) and `Havn-report-sample-90-days.pdf`. They use Inter because Apple's fonts can't be put in the repo; the app itself uses SF Pro.

## 13. Everyday features

**Scan the pharmacy label** (Add Medication, step 1)
- **Build:** `DataScannerViewController` (VisionKit) with `.text()` recognition, or a still photo through `VNRecognizeTextRequest`. Both run on the iPhone. The picture is never saved.
- **What it reads:** name, strength and form; directions (“ONE capsule THREE times a day” becomes 3 times a day, spread over the routine's breakfast, lunch and dinner, or 08:00, 14:00 and 20:00); quantity, which sets the supply and, for a course, the end date; the pharmacy's name and number. The patient's name is ignored.
- **After:** goes straight to Review with a “Filled in from the label” banner. Nothing is added until the person taps Add Medication.
- **Motion:** the corner brackets close in on the label (spring), each line that was read lights up in turn with a selection haptic, then the results sheet rises with a success haptic.

**A photo of the pill** (Add Medication, Shape and Colour)
- Take a Photo opens the camera with a circle to put one pill in; Choose from Photos uses `PhotosPicker`. The photo is cropped square, shrunk to 160 × 160 and stored with the medication. It replaces the drawn pill everywhere, widgets included.
- Camera access needs `NSCameraUsageDescription`. Photos needs no permission with `PhotosPicker`.

**A new medication starts at its next time.** Times earlier today are not added, so a medication added at 13:42 with an 08:00 dose doesn't show as overdue. If every time has passed, it starts tomorrow.

**Daily Routine** (Settings › Daily Routine; Add Medication › Schedule › With Your Routine)
- Five anchors per person: Wake Up, Breakfast, Lunch, Dinner, Bedtime, with optional different times at weekends.
- A medication linked to an anchor moves when the anchor moves, and its reminders are rescheduled. Its page says “With lunch · 13:00”, plus the weekend time.
- Moving a linked medication's time by hand (or accepting a suggestion) unlinks it.

**I Picked Up a Refill and the pharmacy** (a medication's page)
- Refill: pack sizes 28, 30, 56, 90 or any number; shows the new total and roughly when it runs out. The supply goes down only when a dose is logged.
- Pharmacy: name, address and number, kept per person. Call opens the Phone app with `tel:`. Hävn never contacts the pharmacy.

**A gentle suggestion** (Today)
- When a dose is logged at least 30 minutes late on most of the last 14 days, Today offers once to move it: “Move Iron to 13:45?” Move or Not Now. It disappears once that dose is logged.
- Worked out on the iPhone from the dose log. Never about the dose, the strength or whether to take it; only the reminder time.

**Fill Your Pillbox** (Medications)
- A 7-day organiser for the next 7 days, with a row for each part of the day in use. One medication at a time: its compartments light up and the pills drop in (spring, staggered by day).
- If there isn't enough left for the week, it says how many boxes stay empty. Filling doesn't change the supply.
- The card then reads “Pillbox Filled · For 19–25 November”.

## 14. Health, family and beyond the app

**Everyone** (people menu › Everyone, when there are two or more people)
- One card per person: ring, count, and what's due or next. Taken logs that person's due doses without switching. Tapping a card opens that person's Today.
- Reminders name the person: “Time for Mum’s 12:30 dose”. Another person's due doses get their own Lock Screen notification.

**“Did it help?”** (as-needed doses)
- An hour after an as-needed dose, a notification asks: Better, About the Same, Worse (notification actions, no need to open the app). It can also be answered on Today straight away.
- The answers go into the report, next to each as-needed dose.

**Readings** (History; a medication's page)
- Blood pressure (mmHg), blood sugar (mmol/L, or mg/dL by region) and weight (kg or lb). Typed in, or read from Apple Health with read-only access to `bloodPressureSystolic`, `bloodPressureDiastolic`, `bloodGlucose` and `bodyMass`.
- Out-of-range entries are refused with a plain message (blood pressure 60–260 / 30–160, with the upper number higher).
- A medication can show the reading it's for (Amlodipine shows blood pressure). Hävn draws the line and never interprets it.

**How you feel** (a day's note)
- Mood, energy and pain, 1 to 5, each optional; tap the same number again to clear it. Shown on the day and on Today's Notes card.

**The month in rings** (History)
- One mini ring per day, the share of doses taken, the longest run of complete days and the hardest day. Share as Image makes a picture with rings only, never medication names. Not a streak: nothing resets and nothing is lost.

**The report, page 3** (when there are readings or ratings in the period, and Include Readings and Symptoms is on)
- A chart and the last six values for each kind of reading, then a grid of the mood, energy and pain ratings by day.

**Simple Mode** (Settings › Everyday, or Assistive Access)
- One big next dose with I Took It, the day's doses with large checks, and the count. Leaving asks first.
- **Assistive Access:** set `UISupportsAssistiveAccess` and add an `AssistiveAccess` scene (iOS 26) that shows the same Simple Mode view, so Hävn is listed as optimised for Assistive Access.

**Read Names Aloud** (Settings › Everyday)
- When a reminder or alarm rings, it says “Time for your Iron, 65 milligrams.” Hävn renders the sentence to a sound file on the iPhone with `AVSpeechSynthesizer.write(_:toBufferCallback:)` and uses it as that reminder's sound: a CAF or AIFF file under 30 seconds in `Library/Sounds` of the App Group container, set with `UNNotificationSound(named:)`. Re-render when the name, the strength or the language changes. Off by default, because it says the name out loud.

**Alarms** (AlarmKit, iOS 26)
- The alarm's title is the medication and strength. The stop button is Taken and runs `LogDoseIntent`; the secondary button is Snooze (10 minutes, countdown).

**Apple Watch**
- Complications: `.accessoryCircular` (ring) and `.accessoryRectangular` (“Iron · due”, count), from the same WidgetKit extension.
- The app: the ring with the count, the next dose and a green Taken button. Logging on the watch syncs to the iPhone with `WatchConnectivity`, phone to watch and back, never through a server.

**Live Activity and the Dynamic Island** (ActivityKit)
- Starts 30 minutes before a dose and ends when it's logged. Compact: the ring on the left, minutes to go (teal) or late (orange) on the right. Expanded: the medication, the due time, and Snooze and Taken as App Intent buttons. Lock Screen: the same card, with the name hidden unless Show Names on Lock Screen is on.
- Started and updated locally; no push updates, so no server.

**Control Center and the Action Button**
- A `ControlWidgetButton` (iOS 18) running `LogNextDoseIntent`. It logs the next dose only if it's due within the hour or late; otherwise the island says “Next at 22:00” and nothing is logged. The same control can be put on the Action Button.

**Siri and Shortcuts** (App Intents)
- App Shortcuts phrases include the app's name: “What do I take next in Hävn?”, “Log my morning doses in Hävn”, “Did I take my Iron in Hävn?”.
- The answer is an interactive snippet (`SnippetIntent`, iOS 26) with Log It (`Button(intent:)`), shown only for a dose that is due. After logging, the snippet updates in place.

**StandBy**
- The small widget fills the left half (ring and count); the clock and next dose sit on the right. Night mode turns it red automatically in a dark room. The Live Activity can also fill StandBy.

**Focus filters** (`SetFocusFilterIntent`)
- Per Focus, choose who Hävn shows: everyone, You, or one person (for example Caring shows only Mum). Reminders for everyone still arrive. Set up in iPhone Settings › Focus.

**Share a Schedule** (Settings › People)
- Exports one person's medications, times and reminders as a `.havn` file, optionally with history and notes. Sent with AirDrop or saved to Files through `ShareLink`. No server, no account; the other iPhone gets a copy and later changes don't sync.

**Not built, on purpose:** interaction warnings or dose advice, cloud sync, streaks and badges.
