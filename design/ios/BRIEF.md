# Hävn for iOS — design brief and plan

Status: draft for approval. Nothing is designed or built yet.
Date: 18 Nov 2026

---

## 1. The one rule

**Hävn should feel like a page of iOS, not a separate app.**

The test: put a different icon on it and show it to someone. If they would
believe Apple made it, it passes. If they notice "a designer's style", it
fails.

Benchmarks, in order:
1. Apple's own apps: Health (Medications), Fitness, Reminders, Clock.
2. Small-team apps that feel native and still have character: Gentler Streak,
   Structured, Streaks, Bevel.

Hävn's personality comes from **care in the details** (copy, motion, one
signature moment), never from a custom visual style laid over iOS.

---

## 2. Why Hävn exists (decide this first)

Apple's Health app already has Medications (sources at the end):
- log a dose as Taken or Skipped
- dose reminders, plus a follow-up reminder 30 minutes later
- Critical Alerts per medication
- Apple Watch app with "Log All as Taken"
- it lives at Health → Browse → Medications

So Hävn must do something better. My suggestions (product ideas, **not
tested**):

| Gap in Health | What Hävn does |
|---|---|
| Medications is three taps deep | Today is the first screen, the next dose is always one tap away |
| One person per Apple Account | Several people on one phone (a parent, a child, someone you care for) |
| Reminders are notifications | Optional alarm that rings through Silent and Focus (AlarmKit) |
| Few ways to log outside the app | Widgets, Lock Screen, Control Center, Siri and Shortcuts, Watch |
| Setup by hand | Import your list from Health (read-only, with permission) |

---

## 3. Design principles

1. **System first.** Use standard SwiftUI views (NavigationStack, TabView,
   List, Form, sheets, menus). Customise only what makes Hävn Hävn.
2. **Liquid Glass only where Apple puts it.** Bars, buttons, toggles and
   sheets get it automatically. Content (doses, history) stays solid.
   No custom glass cards, no glass on glass (Apple's guidance).
3. **One font family.** San Francisco (the system font) with Dynamic Type
   text styles. Big numbers may use SF Pro Rounded. At most two weights per
   screen.
4. **Colour: 60-30-10.** System backgrounds (60), system greys and labels
   (30), **one** accent colour (10). State colours: taken = system green,
   coming up = accent, skipped = grey, overdue = system orange. Every colour
   has a light, dark and increased-contrast variant.
5. **SF Symbols for every icon**, with the built-in symbol animations
   (bounce, replace). No custom icon set.
6. **Layout like Settings and Health.** Inset grouped lists, 16–20 pt side
   margins, rounded corners that nest inside each other, system fills instead
   of shadows, no decorative background shapes.
7. **Motion from the system.** System springs, the zoom transition into
   detail screens, rolling digits for numbers that change, a success haptic
   when a dose is logged. Everything respects Reduce Motion and Reduce
   Transparency. No skeleton loaders: the data is on the phone and loads
   instantly.
8. **One signature moment.** Logging a dose: the circle fills, the check
   draws on, the "2 of 4" digits roll to "3 of 4", a light success haptic.
   An Undo toast stays for a few seconds. That's the moment people will
   remember; everything else stays quiet.
9. **Apple's words, calm tone.** Use the same words as Health: Taken and
   Skipped (the Watch app also says "Remind Me in 10 Minutes"). Never
   "failed" or "missed streak". A skipped dose says what to do next, not
   that you did wrong.
10. **Hävn reaches outside the app.** Widgets, Lock Screen, Control Center,
    Siri, Watch, alarms. If the user rarely needs to open the app, that's a
    success.

---

## 4. Structure

**Three tabs:** Today · History · Medications.
Settings and profile switcher sit behind the round avatar at the top right
(as in Health and the App Store).

**Tab bar accessory:** the slot above the tab bar, the one Music uses for its
mini player, shows the next dose: "Iron · 1:00 PM · Taken". It's always one
tap away, on every tab. This replaces the centre button idea, because Apple
says the tab bar itself is for navigation, not actions.

---

## 5. Screens and states

| # | Screen | Contents | States to design |
|---|---|---|---|
| 1 | Onboarding (3 steps max) | Welcome · reminders vs alarm permission, explained in one sentence each · add first med or import from Health | permission denied |
| 2 | **Today** | Large title, date · progress summary ("2 of 4 taken") · doses grouped Morning / Afternoon / Evening / Night · "Log All as Taken" per group · as-needed meds at the bottom | morning, overdue, all done, nothing scheduled, first launch |
| 3 | Dose sheet | Taken · Skipped · Remind Me in 10 Minutes · change time taken · note | already taken (edit), late |
| 4 | Add / edit medication | Step-by-step form: name → form → strength → schedule → look (shape, colour) → review | validation, duplicate name |
| 5 | Medications | Inset grouped list: symbol, name, schedule, supply left | empty, refill soon, archived |
| 6 | History | Week strip · calendar · per-medication record · calm summary line | first week, gaps |
| 7 | Settings | People · reminders and alarms · Health access · export and backup · privacy · about | — |
| 8 | Outside the app | Notification with actions · Lock Screen and Home Screen widgets · Control Center control · Live Activity for a dose window · Watch app · alarm screen | Lock Screen hides medication names by default |

---

## 6. Plan, with your approval at each step

| Step | What I deliver | What you do |
|---|---|---|
| 0 | This brief | Answer the questions in section 8 |
| 1 | Foundations sheet: colours (light/dark), type scale, symbols, spacing, motion | Approve or change |
| 2 | **Today** in light and dark, 3 states | Approve. This sets the look for everything else |
| 3 | Remaining screens, two at a time | Approve each pair |
| 4 | Outside-the-app surfaces | Approve |
| 5 | Motion preview of the signature moment (short video) | Approve |
| 6 | SwiftUI build | Needs a Mac with Xcode, which this cloud session doesn't have |

**How the previews will look native this time.** The earlier mockups used a
fallback font because the system font isn't installed here, which is part of
why they looked like a Google app. Next time either:
- I use Apple's official SF Pro font for the mockups, if I can download it
  here, or
- we design in Figma with Apple's official iOS UI kit, if you connect Figma.

---

## 7. The prompt

Copy this into a design tool (Figma Make, Stitch, v0) or give it back to me.
It's written to stand on its own.

```
Design a native iOS 26 app called Hävn, a private medication reminder.

Goal: it must look and behave like an app Apple made. A user should not feel
they have left iOS. Benchmarks: Apple Health (Medications), Fitness,
Reminders, Clock; plus Gentler Streak, Structured, Streaks, Bevel.

Hard rules
- Standard iOS 26 components only: large-title navigation, TabView with
  three tabs (Today, History, Medications), inset grouped lists, sheets,
  menus, toggles, steppers. Liquid Glass appears only on system bars,
  buttons and sheets, never on content cards.
- Font: SF Pro, Dynamic Type text styles only (Large Title, Title 2,
  Headline, Body, Subheadline, Footnote). Big numbers may use SF Pro
  Rounded. Max two weights per screen.
- Colour: system backgrounds and labels plus ONE accent colour [ACCENT].
  Taken = system green, coming up = accent, skipped = system grey,
  overdue = system orange. Light and dark mode for every screen.
- Icons: SF Symbols only (pills.fill, pill, alarm, clock, calendar,
  checkmark.circle.fill, person.crop.circle).
- No shadows, no gradients on content, no decorative shapes, no
  illustrations except one small empty-state symbol.
- 16-20 pt side margins, 8 pt spacing grid, concentric rounded corners.

Screens
1. Today: large title "Today" with the date; a summary "2 of 4 taken" with a
   small progress ring; doses grouped Morning / Afternoon / Evening / Night,
   each row = coloured pill symbol, name, strength, time, state; a
   "Log All as Taken" button on each group; a tab bar accessory above the
   tab bar showing the next dose with a Taken button.
2. Dose sheet: medication name and time; buttons Taken, Skipped,
   Remind Me in 10 Minutes; "Time taken" picker; note field.
3. Add medication: step-by-step form (name, form, strength, schedule,
   shape and colour, review).
4. Medications: inset grouped list with schedule and supply left.
5. History: week strip, month calendar with taken / skipped / partial days,
   one calm sentence summary.
6. Settings: people (profiles), reminders and alarms, Apple Health access,
   export, privacy.

Sample data: Metformin 500 mg 8:00 (taken), Vitamin D3 2,000 IU 8:00
(taken), Iron 65 mg 13:00 (next, in 42 min), Magnesium 200 mg 22:00 (later).

Tone: calm, plain, never blaming. Use Apple Health's words: Taken, Skipped,
Remind Me in 10 Minutes.

Signature moment (describe or prototype): tapping Taken fills the circle,
draws the checkmark, rolls "2 of 4" to "3 of 4", plays a light success
haptic, and shows an Undo toast for a few seconds.

Deliver: Today first, light and dark, in three states (morning, one dose
overdue, all done). Then the other screens.
```

---

## 8. Decisions I need from you

1. **Who is it for?** One person, or also someone managing a family
   member's medication? This decides whether profiles are core or hidden.
2. **Apple Health:** offer import from Health, or stay fully standalone?
3. **Accent colour:** one colour only. Do you have a preference?
4. **Alarms:** offer an optional alarm (rings through Silent) per medication?
5. **Minimum iOS version:** iOS 26 and later (simplest, has Liquid Glass,
   AlarmKit and the Health medications access), or support older iPhones
   too? I haven't checked how many iPhones run iOS 26.

---

## 9. Verified vs not

Checked against sources this session:
- Apple Health Medications features: support.apple.com/en-us/105064 and the
  iPhone and Watch user guides.
- Liquid Glass rules (use sparingly, no glass on glass, colour judiciously,
  tab bars are for navigation, tab bar accessory like Music's mini player):
  developer.apple.com "Adopting Liquid Glass", HIG "Tab bars".
- AlarmKit (breaks through Silent and Focus, Lock Screen, Dynamic Island,
  StandBy, Watch, per-app opt-in): WWDC25 session 230.
- HealthKit Medications API (read access per medication, includes dose
  events): WWDC25 session 321 and a developer write-up. I found no sign that
  apps can **write** doses back to Health, so treat it as read-only until
  confirmed.
- Live Activities: short tasks with a clear end; avoid sensitive information
  on the Lock Screen: HIG "Live Activities".
- Skeleton screens add nothing under 1 second: Nielsen Norman Group.
- 60-30-10 colour, two fonts, more spacing, borders over shadows, no
  decorative shapes: the ui.martin posts you shared (design advice, not
  research).

Not checked:
- Whether Hävn's "gaps" in section 2 are what users actually want.
- iOS 26 adoption numbers.
- Exact SwiftUI API names; they get confirmed at build time.
