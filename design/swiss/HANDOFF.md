# Hävn — Design & Motion Handoff

Self-contained spec for implementing the visual + motion layer. No prior context needed.

**Reference sketch:** https://claude.ai/artifact/DEUdJx7hKUBYM6rxaTb8qJ

---

## 1. The direction

**Severe Swiss / Braun-functionalist.** Not "Scandinavian cozy."

The reference point is Teenage Engineering and Dieter Rams at Braun — reduction,
symmetry, balance; hard edges, system type, one signal colour. Deliberately
unsentimental. A medication app that treats the user as competent rather than
fragile, which is the opposite of the Headspace/Calm register.

The failure mode to avoid is **half-Swiss**: white background, a grotesque, a
vague grid, and none of the rigor. That reads as dull. Severity is what makes
it work — commit to it.

### Non-negotiables

- One grotesque + one mono. Mono for every label, uppercase, tracked.
- Hard rules (2dp) and hairlines (1dp) for structure. **No cards, no elevation.**
- `cornerRadius = 0` everywhere.
- Flush left. Never centred, never justified.
- Extreme scale contrast — nothing between 17sp and 34sp.
- **Ink + one signal colour.** Nothing else. Taken-state is ink, not a second hue.
- One hero element per screen at 4–5× everything else.

### Why extreme hierarchy (not just taste)

Bentley et al., *Usability Hasn't Peaked*, CHI 2026 — 48 participants, 10 apps,
Material 3 Expressive vs. the flatter previous system: **33% faster fixation on
the correct element, 20% faster task completion, and higher aesthetic ratings.**
Flat uniformity makes people hunt. Give the eye one destination per screen.

---

## 2. Tokens

Semantic names only. Never reference a raw hex in a screen.

### Light

| Token | Hex |
|---|---|
| `paper` | `#F2F1EC` |
| `surfaceRaised` | `#FBFAF7` |
| `ink` | `#14140E` |
| `ink2` | `#5A5B50` |
| `ink3` | `#8E8F85` |
| `rule` | `#D3D2C9` |
| `signal` | `#C23A05` |
| `taken` | `#14140E` *(= ink)* |

### Dark

| Token | Hex |
|---|---|
| `paper` | `#121210` |
| `surfaceRaised` | `#1A1A17` |
| `ink` | `#EDECE4` |
| `ink2` | `#9C9D93` |
| `ink3` | `#6A6B62` |
| `rule` | `#2A2A26` |
| `signal` | `#FF5A1F` |
| `taken` | `#EDECE4` *(= ink)* |

Dark mode is **not an inversion** — note the ground is warm charcoal `#121210`,
never `#000`, and body text sits at ink, not pure white. Light and dark are
genuinely asymmetric (Andrew et al., UbiComp 2024); treat dark as its own
designed room, lamp-lit.

**Variable-font tip:** compensate for text looking heavier on dark using font
**grade**, not weight — grade adds apparent boldness without changing character
width, so nothing reflows when the theme flips (Palmén et al., CHI 2023, n=459).

---

## 3. Type

Archivo (grotesque) + IBM Plex Mono.

| Role | Size / weight | Notes |
|---|---|---|
| Hero numeral | 96 / 700 | `tnum`, tracking −0.055em |
| Screen title | 34 / 700 | tracking −0.035em |
| Row title | 17 / 600 | |
| Body | 15 / 400 | |
| Label (mono) | 11 | uppercase, tracking 0.13em |
| Nav / caption (mono) | 9 | uppercase, tracking 0.10em |

**Nothing between 17 and 34.** That gap is the whole hierarchy.

`fontFeatureSettings = "tnum"` anywhere digits change in place, or the layout
shivers every time the countdown ticks.

---

## 4. Structure

| | |
|---|---|
| Page gutter | 20dp, fixed, never varies |
| Spacing scale | 4 / 8 / 12 / 20 / 32 / 56 |
| Hairline | 1dp `rule` |
| Hard rule | 2dp `ink` |
| Corner radius | 0 |
| Elevation | none — rules only |
| Numerals | tabular everywhere |
| Alignment | flush left |

---

## 5. Screens

### Today
- Hero: **time until next dose** ("42 min"), the one thing people open the app for.
- Dose rows, hairline-divided: mono time · name + dose · square checkbox.
- **Next dose is a full-bleed `signal` bar** — the only coloured object on screen.
- Bottom nav: four mono words, 2dp signal tick over the active one. No icons.

### History
- 7 columns × 9 weeks matrix. Taken = solid ink. Partial = ink @ 35%.
  **Missed = signal *outline*, never a fill** — visible, not punishing.
- Hero: adherence percentage.
- Keep it to 9 weeks so the nav bar stays in frame.

### Add / edit medication
- A spec sheet, not a form. Mono key above, large value below, hairline between.
- No input chrome, no floating labels, no cards. Row states a fact; tap opens a sheet.

---

## 6. Motion

Mechanical. **Snaps to position. Shutter, not rubber band.** No springs, no
overshoot — springy bounce is the warm-Nordic idiom and fights this direction.

```kotlin
object Motion {
    val Decisive = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val Exit     = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    const val State    = 140   // checkbox, toggle, press
    const val Nav      = 220   // screen change
    const val Sheet    = 260   // bottom sheet
    const val Stagger  = 28    // per-row delay
    const val CellWave = 12    // per-diagonal delay in the matrix
}

/** Android's reduced-motion signal. Honour it. */
@Composable
fun rememberReducedMotion(): Boolean {
    val ctx = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        ) == 0f
    }
}
```

Animated transitions are functional, not decoration — they increase perceived
continuity, guide attention, and help users build accurate mental models
(Mackamul et al., CHI 2025). But badly-tuned animation measurably *damages*
engagement (Wu et al., CHI 2020), so keep to the spec above.

### 6.1 Staggered reveal — highest value per line of code

```kotlin
@Composable
fun Reveal(index: Int, content: @Composable () -> Unit) {
    val reduced = rememberReducedMotion()
    var shown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!reduced) delay(index * Motion.Stagger.toLong())
        shown = true
    }

    val a by animateFloatAsState(
        if (shown) 1f else 0f,
        tween(if (reduced) 0 else Motion.State, easing = Motion.Decisive),
        label = "revealAlpha"
    )
    val y by animateDpAsState(
        if (shown) 0.dp else 8.dp,
        tween(if (reduced) 0 else Motion.State, easing = Motion.Decisive),
        label = "revealY"
    )

    Box(Modifier.graphicsLayer { alpha = a; translationY = y.toPx() }) { content() }
}
```

**8dp of travel, not 40.** A settle, not a swoop.

⚠️ Use a plain `Column` for the ~5 dose rows. In a `LazyColumn`,
`LaunchedEffect(Unit)` re-fires when items scroll back into view and the stagger
replays. If you see it replaying on nav-back, hoist `shown` into the ViewModel.

### 6.2 Checkbox fill — the signature move

One gesture, performed daily. This is what people remember.

```kotlin
@Composable
fun DoseCheckbox(checked: Boolean, ink: Color, paper: Color, onToggle: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val fill  by animateFloatAsState(if (checked) 1f else 0f,
        tween(Motion.State, easing = Motion.Decisive), label = "fill")
    val scale by animateFloatAsState(if (pressed) 0.975f else 1f,
        tween(80), label = "press")

    Canvas(
        Modifier.size(20.dp).scale(scale)
            .clickable(interaction, indication = null) {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                onToggle()
            }
    ) {
        val s = 1.5.dp.toPx()
        drawRect(ink, style = Stroke(s))

        // Ink wipes UP from the bottom edge — a hard wipe, not a fade.
        if (fill > 0f) {
            drawRect(ink,
                topLeft = Offset(0f, size.height * (1f - fill)),
                size = Size(size.width, size.height * fill))
        }
        // Check appears only once the box is mostly filled.
        if (fill > 0.65f) {
            val p = Path().apply {
                moveTo(size.width * .22f, size.height * .52f)
                lineTo(size.width * .42f, size.height * .72f)
                lineTo(size.width * .80f, size.height * .28f)
            }
            drawPath(p, paper, style = Stroke(s * 1.2f, cap = StrokeCap.Square))
        }
    }
}
```

`StrokeCap.Square`, not `Round`. Rounded caps are the friendly register we're avoiding.

### 6.3 Matrix reveal — the showpiece

Stagger **diagonally** so it reads as a wave crossing the grid.

```kotlin
@Composable
fun MatrixCell(row: Int, col: Int, content: @Composable () -> Unit) {
    val reduced = rememberReducedMotion()
    var shown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Diagonal wave: delay grows with (row + col), not row alone.
        if (!reduced) delay(((row + col) * Motion.CellWave).toLong())
        shown = true
    }

    val a by animateFloatAsState(
        if (shown) 1f else 0f,
        tween(if (reduced) 0 else 120, easing = Motion.Decisive),
        label = "cell"
    )

    Box(Modifier.alpha(a).aspectRatio(1f)) { content() }
}
```

Last cell lands ~170ms. Fast enough to never annoy on the hundredth visit —
which is the real test of an entrance animation.

### 6.4 Countdown

```kotlin
AnimatedContent(
    targetState = minutesRemaining,
    transitionSpec = {
        (slideInVertically { it / 3 } + fadeIn(tween(Motion.State))) togetherWith
        (slideOutVertically { -it / 3 } + fadeOut(tween(Motion.State)))
    },
    label = "countdown"
) { m ->
    Text(
        "$m",
        style = MaterialTheme.typography.displayLarge,   // the 96/700 hero style
        fontFeatureSettings = "tnum"
    )
}
```

### 6.5 Screen transitions

```kotlin
enterTransition = {
    slideInHorizontally(tween(Motion.Nav, easing = Motion.Decisive)) { it / 4 } +
        fadeIn(tween(Motion.Nav))
}
```

Quarter-width, not full. Full-width slides feel like iOS; this isn't that.

### 6.6 Do NOT animate

- **The signal bar moving between rows — it must CUT.** When a dose is logged the
  bar jumps to the next one instantly. Animating it makes the app feel slow at
  the exact moment the user wants confirmation.
- **Tab switches** — cross-fade content at 100ms. No sliding between tabs.
- **Anything on every return to a screen.** Entrance animations belong to app
  open, not to nav-back.

### 6.7 Haptics

`HapticFeedbackType.Confirm` on every committing action — logging a dose above
all. One line, and nothing else says "well made" as fast.

---

## 7. Onboarding

Four screens. **Build this LAST**, after every real screen exists — screen 3
renders the actual Today screen, so it cannot be built before Today is done.
(Teams that build onboarding first invariably rebuild it.)

1. **Name.** One field, skippable. Big type, one question.
2. **First medication.** The real form, not a mock.
3. **It renders.** Their medication in the real Today screen. Nothing asked of
   them. This is the payoff beat.
4. **Reminders?** → *then* the notification permission dialog.

Permission comes last, immediately after the moment they obviously want it.
Never on screen one. Persist `onboarding_complete` in DataStore.

No carousel. The privacy line ("no account, no network") deserves its own full
screen with four words on it — it's the most honest thing the app can say.

---

## 8. Discoverability warning

Severe minimalism has one real trap: **hiding controls for cleanliness.**
A gesture with no visual cue is a *hidden affordance* and users do not find it
(GhostUI, CHI 2026). Every gesture needs a visible companion. If you remove a
control to keep a screen clean, it is gone, not subtle.

Related: on any wait, don't show a precise countdown you might finish early —
premature completion erodes trust (Tan & Nov, CHI 2026). Use elapsed time or a
qualitative status cue.

---

## 9. Avoid

Pure `#FFF` / `#000` · Inter (overexposed) · centred layouts · equal-weight
cards in a stack · illustration-heavy empty states · gradients · glassmorphism ·
any animation that bounces more than once · decorative serif display type.

---

## 10. Version flags — check before pasting

- `HapticFeedbackType.Confirm` needs **Compose Foundation 1.7+**. Below that use
  `HapticFeedbackType.LongPress`.
- `togetherWith` replaced the infix `with` in **Compose 1.5**.
- `label` params on `animateFloatAsState` are newer — a warning, not an error.

None of the Kotlin in this document has been compiled. Check it against the
project's version catalog on first paste.
