# Hävn Medication Reminder — design direction

The pill box becomes a **glass capsule with seven shelves**, one per day, shot
like a product on a studio sweep. Paper, ink and the olive from the icon. One
type family. Cuts and dissolves at film pace, 24 fps grain, glass glint, a light
leak for the title card.

Everything visual lives in one file: **`havn-medication-reminder.html`**. Open it in any
desktop browser with WebGL 2. Every capsule, pill and shader in it is a live
render from the same model that produced the assets in `assets/`, so the app's
bitmaps match the design exactly.

| Reel | What it holds |
| --- | --- |
| R1 Brief | What changes, references, house rules |
| R2 Object | The capsule, live and scrubbable through the week |
| R3 Colour | Matinée (light), Late show (dark), pill materials |
| R4 Type | Hanken Grotesk only — no new fonts |
| R5 Light | Four AGSL shaders with live WebGL previews |
| R6 Motion | Curves, six playable motion studies, the timing table |
| R7 Assets | Pill specimens, glass layers, grain, icons, widget stub |
| R8 Frames | Eleven app screens at 360 × 760 dp |
| R9 Build | The order to implement it (expanded below) |

```
design/
├── havn-medication-reminder.html   the design file (also holds the AGSL sources)
├── shaders/                 grain.agsl · sweep.agsl · glint.agsl · leak.agsl
├── assets/
│   ├── capsule/             glass tint + light layers, shadow, mask, capsule_layout.json
│   ├── pills/               36 sprites: 6 forms × 6 materials, at capsule scale
│   ├── specimens/           6 form cards for New medication
│   ├── texture/             grain_256.png
│   ├── store/               feature-graphic-1024x500.png
│   └── icons/               9 hairline icons, 24 dp, 1.5 stroke, square ends
└── tools/export-assets.mjs  re-renders everything above
```

Regenerate assets after changing the model: `node design/tools/export-assets.mjs`
(needs Playwright + Chromium; pass a substring to export a subset, e.g. `pills`).
For any other still, open the design file and run
`havnExport.render({ pal: 'late', el: 0.3, at: 61 }, 1080, 1920)` in the console;
it returns a PNG data URL (`at` is hours since Monday 00:00).

---

## The object

- **Seven shelves, Monday at the top.** The week drains downward like an
  hourglass: past shelves are empty, today is part-full, the rest are loaded.
- **Up to six doses per shelf**, spread left to right in time order.
  `capsule_layout.json → shelves[d].slots[n]` gives every contact point.
- **Taken:** the dose lifts 0.035 units and dissolves over 700 ms; an etched
  ellipse stays where it lay.
- **Missed:** the dose stays on its shelf. The capsule never hides a missed dose.
- **Today:** a thin olive bracket beside the etched scale (`todayMark`). A
  focused shelf gets an olive rim (`rim`).
- **How much:** shelf labels read `D-03 Wed 2 / 5`; the total sits under the
  capsule: `11 of 35 taken`.

Example schedule used everywhere: Wednesday 14 October 2026, 13:00.
08:00 Metformin 500 mg · 08:00 Vitamin D3 2,000 IU · 13:00 Iron 65 mg ·
19:00 Metformin 500 mg · 22:00 Magnesium 200 mg. Monday's iron was missed.

---

## Build order

Each step ships on its own and leaves the app working.

### 1 · Tokens — `ui/theme/HavnColors.kt`, `HavnTokens.kt`

Add the palette under its own name so it does not collide with the current
`Ink60`/`Paper10` ladders, then point `HavnColorTokens` at it.

```kotlin
object Cine {
    // Matinée
    val Paper      = Color(0xFFEEE9E0)   // canvas, never pure white
    val PaperDeep  = Color(0xFFE6E0D5)
    val SweepHigh  = Color(0xFFE9E4DA)
    val SweepMid   = Color(0xFFDAD3C7)
    val SweepLow   = Color(0xFFC9C1B3)
    val Ink        = Color(0xFF171612)   // 15.0 : 1 on Paper
    val Ink60      = Color(0xFF5C574E)   //  5.9 : 1
    val Ink40      = Color(0xFF8A8478)   //  3.1 : 1 — 24 sp+ or non-text only
    val Rule       = Color(0xFFCFC8BB)
    val Olive      = Color(0xFF5E6E5D)   //  4.5 : 1 — marks, rims, active tab
    val OliveInk   = Color(0xFF4B5A4A)   //  6.1 : 1 — small olive text
    // Late show
    val Screen     = Color(0xFF12110E)
    val Velvet     = Color(0xFF1A1814)
    val VelvetHigh = Color(0xFF2A2722)
    val VelvetMid  = Color(0xFF1D1B17)
    val VelvetLow  = Color(0xFF100F0D)
    val Bone       = Color(0xFFECE6DA)   // 15.2 : 1 on Screen
    val Bone60     = Color(0xFFABA497)   //  7.6 : 1
    val Bone40     = Color(0xFF8A8478)
    val RuleDark   = Color(0xFF2F2B25)
    val OliveLift  = Color(0xFFA3B39A)   //  8.5 : 1
    val OliveInkDk = Color(0xFFB4C3AB)
}
```

Mapping (Matinée / Late show): canvas and surface → Paper / Screen;
surfaceRaised and surfaceSunken → PaperDeep / Velvet; hairline → Rule / RuleDark;
hairlineStrong → Ink / Bone; textPrimary → Ink / Bone; textSecondary →
Ink60 / Bone60; textTertiary → Ink40 / Bone40; accent → Olive / OliveLift;
accentPressed and onAccentSoft → OliveInk / OliveInkDk; onAccent → Paper / Screen;
ambientA/B → SweepHigh, SweepMid / VelvetHigh, VelvetMid.
Olive is the only colour: positive → Olive; warning and danger → Ink / Bone with
plain words ("Delete medication"), not red. Clay, Amber, Slate and Sand stop
being UI colours and live on only as pill materials.

### 2 · Type — `ui/theme/HavnTypography.kt`

Every style uses `HankenGrotesk`. Remove `EditorialSerif` and delete
`res/font/instrument_serif.ttf` and `instrument_serif_italic.ttf`. Add a
`fontFeatureSettings` parameter to the `sans()` helper.

| Role | M3 slot | Weight | Size / line | Tracking | Notes |
| --- | --- | --- | --- | --- | --- |
| Numeral | displayLarge | Medium | 92 / 84 | −5.0 sp | `tnum` — the time on a dose |
| Display | displayMedium | SemiBold | 56 / 52 | −2.5 sp | Screen statements |
| Title card | displaySmall | SemiBold | 44 / 42 | −2.0 sp | F01 |
| Headline | headlineLarge | SemiBold | 34 / 36 | −1.2 sp | Section titles |
| Headline S | headlineSmall | SemiBold | 22 / 26 | −0.6 sp | Counts under the capsule |
| Body | bodyLarge | Regular | 16 / 24 | 0 | Explanations |
| Body S | bodyMedium | Regular | 14 / 20 | 0 | Secondary lines |
| Credit | labelMedium | Medium | 15 / 22 | 0 | `tnum` — every dose |
| Button | labelLarge | SemiBold | 15 / 20 | −0.1 sp | |
| Label | labelSmall | SemiBold | 11 / 14 | +1.8 sp | Caps in code (`uppercase()`) |

Credit format: `08:00 — Metformin — 500 mg`. Spaced em dashes in Ink40, tabular
figures, no colons, no bullets. Technical codes always sit next to a plain word:
`H-01 / Today`, `D-03 Wed`, `F-02 Softgel`.

### 3 · Motion — `ui/theme/HavnMotion.kt`

```kotlin
object HavnMotion {
    val Projector: Easing = CubicBezierEasing(0.45f, 0f, 0.15f, 1f)  // dissolves, credits, take
    val Dolly: Easing = CubicBezierEasing(0.30f, 0f, 0.20f, 1f)      // camera into a shelf

    const val FadeOut = 240        // anything leaving, linear
    const val Dissolve = 480       // screens, sheets, dose
    const val Credit = 520         // list lines, + 60 ms stagger, 12 dp rise
    const val CreditStagger = 60
    const val Counter = 360
    const val Take = 700           // ring fades in during the last 300 ms
    const val Focus = 900
    const val TitleIn = 900
    const val TitleHold = 1600
    const val TitleOut = 1200
    const val Drift = 26_000       // glint loop on Capsule

    fun <T> dissolve(): FiniteAnimationSpec<T> = tween(Dissolve, easing = Projector)
    fun <T> fadeOut(): FiniteAnimationSpec<T> = tween(FadeOut, easing = LinearEasing)
    fun <T> take(): FiniteAnimationSpec<T> = tween(Take, easing = Projector)
    fun <T> focus(): FiniteAnimationSpec<T> = tween(Focus, easing = Dolly)
}
```

- Tabs cut: `EnterTransition.None togetherWith ExitTransition.None`.
- Pushed screens, sheets and the dose view dissolve:
  `fadeIn(dissolve()) togetherWith fadeOut(fadeOut())`. No slides anywhere.
- Delete `press()`, `settle()`, `glide()` and `celebrate()`. While migrating you
  can point them at `tween(160, easing = Projector)` so call sites compile.
- Reduced motion (`Settings.Global.ANIMATOR_DURATION_SCALE == 0` or the system
  setting): opacity changes at 150 ms, everything else instant, grain frozen.

### 4 · Assets

Run the exporter, then copy:

- `assets/capsule/*.png`, `assets/pills/*.png`, `assets/specimens/*.png`,
  `assets/texture/grain_256.png` → `app/src/main/res/drawable-nodpi/`
- `assets/capsule/capsule_layout.json` → `app/src/main/assets/`
- `shaders/*.agsl` → `app/src/main/res/raw/`
- `assets/icons/*.svg` → import as vector drawables (Resource Manager)
- `assets/store/feature-graphic-1024x500.png` → `play-store/graphics/`

### 5 · Capsule — `ui/components/HavnCapsuleView.kt`

Replaces `HavnOrganizerView` in `HomeScreen`, `OrganizerScreen` and
`OnboardingScreen`. It needs a week of doses, not a day:

```kotlin
enum class PillForm { OVAL, SOFTGEL, ROUND, CAPSULE, VIAL, SACHET }      // F-01 … F-06
enum class PillMaterial { CHALK, BONE, OLIVE, INK, AMBER, RUST }        // M-01 … M-06

@Immutable data class CapsuleDose(
    val dose: TodayDose,
    val form: PillForm,
    val material: PillMaterial,
    val missed: Boolean,       // pending and past its time + grace on a past day or earlier today
)
@Immutable data class CapsuleWeek(
    val shelves: List<List<CapsuleDose>>,   // [0] = Monday, each sorted by time, max 6 shown
    val today: Int,                          // 0 = Monday
)
```

- Repository: add `getDoseLogsBetween(userId, from, to)`; build each day with the
  same rules as `OrganizerViewModel.buildDoses` and `Medication.isScheduledOn`.
- Forms: `MedIconType` CAPSULE → CAPSULE, TABLET → ROUND, LIQUID → VIAL,
  INJECTION → VIAL, POWDER → SACHET. Add `OVAL` and `SOFTGEL` to `MedIconType`
  **at the end** of the enum (check the Room converter before relying on names).
- Materials from `colorTag`: sage → OLIVE, clay → RUST, amber → AMBER,
  slate → INK, sand → BONE, anything else → CHALK. Add `chalk` as the default.
- More than six doses on a day: show six and an etched `+2` in the label.

Draw it in one `Canvas` with `aspectRatio(0.5f)`, in the 1200 × 2400 frame of
`capsule_layout.json`, scaled to the view:

```kotlin
Canvas(modifier.aspectRatio(0.5f).pointerInput(layout) {
    detectTapGestures { p -> layout.shelfAt(p.y / size.height * 2400f)?.let(onShelfTapped) }
}) {
    val k = size.width / 1200f
    drawSweep()                                             // S-02, or gradient fallback
    scale(k, pivot = Offset.Zero) {
        drawImage(art.shadow)                                // ink with alpha
        drawImage(art.backTint, blendMode = BlendMode.Multiply)
        drawImage(art.backLight, blendMode = BlendMode.Plus)
        focus?.let { drawOval(rim(it), Cine.Olive, style = Stroke(3f)) }
        drawRect(Cine.Olive, todayMark(week.today))
        week.shelves.forEachIndexed { d, doses ->
            val slots = layout.shelves[d].slots[doses.size]
            doses.indices.sortedBy { slots[it].depth }.forEach { i ->
                drawDose(doses[i], slots[i], progress[doses[i].dose])   // sprite, lift, alpha, ghost ring
            }
        }
        drawImage(art.frontTint, blendMode = BlendMode.Multiply)
        drawImage(art.frontLight, blendMode = BlendMode.Plus)
    }
}
```

- `drawDose`: sprite top-left = slot − `sprite.anchor`; lift = `0.035 × pixelsPerUnit × p`;
  alpha = `1 − p`; ghost ring = ellipse `rx`, `rz × sin(shelf.viewElevation)`,
  rotated by `yaw`, 2 px, alpha `((p − 0.57) / 0.43).coerceIn(0f, 1f)`.
  Ring colour: Ink at 20 % on Matinée, Bone at 35 % on Late show.
- Missed doses draw as normal pills. They never fade.
- Shelf focus (M-03): animate a `graphicsLayer` scale to 1.9 around the shelf's
  `rim` centre with `HavnMotion.focus()`; back reverses it.
- Load the bitmaps once (`ImageBitmap.imageResource`) and the JSON once
  (`kotlinx.serialization`). The view itself allocates nothing per frame.

### 6 · Shaders — `res/raw/*.agsl`

```kotlin
@RequiresApi(33)
fun Context.runtimeShader(@RawRes id: Int) =
    RuntimeShader(resources.openRawResource(id).bufferedReader().use { it.readText() })
```

| Pass | Where | API 33+ | API 26–32 |
| --- | --- | --- | --- |
| S-01 Grain | Overlay over every screen | `drawRect(ShaderBrush(grain), blendMode = BlendMode.Overlay)`; `frame = floor(s × 24)`; amount 0.10 Matinée, 0.14 Late show; `grainPx = 0.5 × density` | `grain_256.png` as a repeating `ImageShader`, random offset every 1/24 s, Overlay at alpha = amount |
| S-02 Sweep | Today, Capsule, Dose backgrounds | `ShaderBrush`; `setColorUniform("wall", SweepHigh.toArgb())` etc.; horizon 0.62 Today, 0.78 Capsule | `Brush.verticalGradient` of the three sweep colours + a radial pool |
| S-03 Glint | The capsule layer | `graphicsLayer { renderEffect = RenderEffect.createRuntimeShaderEffect(glint, "glass").asComposeRenderEffect() }`, `setInputShader("mask", BitmapShader(mask))` scaled by `k`; phase from scroll (Today) or the 26 s loop (Capsule) | None |
| S-04 Leak | Title card → Today, day-complete end card | Same as S-03 on the screen container, `progress` 0 → 1 over 1200 ms Projector | Plain dissolve |

Pause grain and glint when the screen is not resumed or Battery Saver is on.

### 7 · Screens (R8 frames)

| Frame | Screen | Notes |
| --- | --- | --- |
| F01 | Onboarding | Late show title card, capsule loaded for the week, Begin. M-06 into Today. |
| F02 / F09 | Today | Capsule cropped big and off-centre right on the sweep (top 372 dp); statement `Three left today.`; credits list; `Take 13:00 — Iron`. |
| F03 | Capsule | Whole capsule left, shelf labels as credits with leader lines; `11 of 35 taken`. |
| F04 | Shelf | After a tap and M-03: the shelf close up, numbered positions left to right. |
| F05 | Dose | Specimen on the sweep, 92 sp time, Take / In 15 min / Skip. Dissolves in. |
| F06 | History | 30 days as a contact sheet: a frame per day, a mark per dose, hollow = missed. Replaces Progress. |
| F07 | Reminders | Times set large like a showtimes board, one row per time. |
| F08 | New medication | Form picked from rendered specimens, then material. |
| F10 | Widgets | 4 × 1 ticket stub (`Next 13:00 — Iron — 65 mg`, Taken) and a 2 × 2 capsule tile from a pre-rendered bitmap (Glance cannot run shaders). Notification: title `13:00 — Iron — 65 mg`, actions Mark taken / In 15 min. |

Tab bar: four text labels in Label style (Today, Capsule, History, Settings), the
active one in ink with a 2 dp olive bar above it. No icons in the tab bar.

### 8 · Check

Extend `LaunchSmokeTest` to render Today, Capsule and Dose in both themes and
put them beside F02–F09. Done means:

- Nothing on screen uses a colour outside R3 except the pills.
- No serif anywhere; every time and dose is tabular.
- No spring, overshoot or slide in any transition.
- A taken dose leaves a ring; a missed dose stays; Monday is the top shelf.
- Body text contrast ≥ 4.5 : 1 in both themes.

### Opening type (F00, M-07)

Every time the app opens, before Today: the next dose time and what is left,
set huge (`13 / :00` at 262 sp, bleeding off the right edge, `Three left.` at 118 sp, SemiBold, −7 % tracking,
tabular). Hold 600 ms, then scale the type 1 → 0.2 towards the Today headline
over 700 ms with `Dolly` easing while it fades, and dissolve Today in beneath.
No slide, no bounce. Reduced motion skips straight to Today. Nothing due today:
show `All done.` instead of the time.
