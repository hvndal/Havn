# Kickoff prompt: paste this into the implementing agent

You are a senior Android engineer with a product designer's eye, working in the
Hävn repository (native Android: Kotlin, Jetpack Compose, Hilt, Room, Glance).
Hävn is a calm, offline, private medication organiser whose hero is a
four-compartment pill box drawn on a Canvas.

Your job is to make Hävn feel like a premium, luxury-grade app by making the
**pill box itself the interface**. Users take a dose by touching the pill: it
lifts out, flies to the day's progress line, and the empty compartment closes
itself. Every beat has a matching haptic and sound.

Before writing any code:
1. Read `docs/premium/BRIEF.md` completely. It is the source of truth.
2. Look at every image in `docs/premium/assets/` and watch
   `docs/premium/assets/take-moment-with-sound.mp4`. These are the target.
3. Read `docs/premium/concept/box.js`. It is the reference renderer for the
   frosted-lid materials, sculpted pills and the flight arc. Port it to
   `app/src/main/java/com/havn/app/ui/components/HavnOrganizerView.kt`.
4. Read `HomeScreen.kt`, `HomeViewModel.kt`, `HavnPress.kt` (haptics),
   `audio/SoundManager.kt`, `HavnMotion.kt` and
   `app/src/test/java/com/havn/app/LaunchSmokeTest.kt`.

Then work through the brief's §4 in order: P1 (touchable pills and the take
moment) → P2 (materials) → P3 (lid physics) → P4 (scrub the day) → P5 (Today
layout) → P6 (haptics) → P7 (sounds) → P8 (shared-element detail) → P9 (day
complete) → P10 (first launch) → P11 (notification and widget) → P12 (reduce
motion, accessibility, performance). P13 is optional and needs human
specialists.

Rules:
- One item per commit or PR, and keep each one small. After each, run
  `./gradlew :app:assembleDebug :app:testDebugUnitTest`. Extend
  `LaunchSmokeTest` to render the new state, and compare its PNGs in
  `app/build/screenshots/` with `docs/premium/assets/`. Report differences
  honestly; don't call something done that you haven't rendered and looked at.
- Data writes happen at touch-commit, never at animation end. Undo must work.
- Everything must work with TalkBack: every pill gets a semantics node with a
  Take action. Everything must work with reduce motion, and in silent mode.
- No network, no analytics, no new permissions, no drug-interaction features.
  Don't change reminder scheduling semantics or the Room schema without a
  migration and a test.
- Keep the identity: white page, olive `#5E6E5D`, linen, Instrument Serif and
  Hanken Grotesk, hairlines. This is a feel pass, not a rebrand.
- The draft sounds in `docs/premium/assets/sound/` must be auditioned by a
  human before they replace anything. Copy them into `res/raw/` as new files
  only.

Start with P1. First propose the state model, `OrganizerAnimator` and the
pill hit-area overlay in a short plan. Then implement, test, render, compare,
and commit.
