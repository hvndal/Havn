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
| Today | morning · overdue · all done (ring closed) · empty · reminders off (banner) · another person (Mum) · new person (empty) · as-needed logged · as-needed too soon (alert) · daily limit reached · running low |
| Add Medication (6 steps) | name required · duplicate name warning · strength required · every day / specific days / every few days / cycle / as needed · several times a day · notification or alarm · ends on a date · shape and colours · review |
| Dose sheet | due · overdue · already taken |
| History | week of mini rings · calm summary sentence · month calendar (all / some / skipped) · previous month · day sheet · logging a forgotten dose later · first week (empty) |
| Medications | list with supply · running low · as needed · archived · edit (archive) · detail with stats · not scheduled today |
| Settings | people · reminders · follow-up · alarms · names on Lock Screen · sounds and haptics · Health · Face ID lock · iPhone backups · export · restore · erase (confirmation) · privacy policy · about and disclaimer |
| App lock | locked · Face ID success |
| Home Screen widgets | small · medium (Taken button works without opening the app) · large · all done · Clear (glass) look · dark |
| Lock Screen | ring and next-dose widgets · notification with names hidden · held open with Taken / Remind Me in 10 Minutes / Skipped |
| Accessibility | largest text · Reduce Transparency · Reduce Motion · Increase Contrast |

**Still to design before launch** (no prototype yet): Live Activity for a dose window, the Control Center control, StandBy, the Apple Watch app, Siri and Shortcuts phrases, time-zone travel, daylight-saving changes, tapering doses.

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
