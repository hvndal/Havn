# Hävn — Visual Assets & Motion Design Documentation

This document records all custom visual assets, vector illustrations, micro-graphics, icons, and motion interactions integrated into Hävn during the visual polish pass.

---

## 🎨 Design Philosophy & Goals

- **Aesthetic**: Warm Luxury / Warm Minimalism (Muji, Kinfolk, Scandinavian industrial design).
- **Core Principles**: Subtle premium polish, high readability, responsive feedback, accessibility-first motion, and zero visual noise.
- **License Compliance**: All vector drawables and visual assets are original code assets released under the Apache License 2.0 / Public Domain for commercial and public use. No copyrighted third-party images or random web assets were introduced.

---

## 🖼️ Vector Asset Inventory

| Asset Identifier | File Path | Type | License | Description / Context |
| :--- | :--- | :--- | :--- | :--- |
| `il_onboarding_ritual` | `app/src/main/res/drawable/il_onboarding_ritual.xml` | Vector Illustration | Apache 2.0 (Original) | Onboarding welcome illustration featuring a ceramic dish, botanical leaves, and warm sun accent. |
| `il_onboarding_cadence` | `app/src/main/res/drawable/il_onboarding_cadence.xml` | Vector Illustration | Apache 2.0 (Original) | Onboarding illustration representing the weekly pill organizer tray and completion status. |
| `il_onboarding_mindful` | `app/src/main/res/drawable/il_onboarding_mindful.xml` | Vector Illustration | Apache 2.0 (Original) | Onboarding illustration displaying balanced ceramic stones and floating botanical leaves. |
| `il_empty_today` | `app/src/main/res/drawable/il_empty_today.xml` | Vector Illustration | Apache 2.0 (Original) | Home screen empty-state graphic showing a ceramic cup with subtle aroma swirls and resting leaf. |
| `il_empty_history` | `app/src/main/res/drawable/il_empty_history.xml` | Vector Illustration | Apache 2.0 (Original) | History screen empty-state graphic illustrating an open wellness journal with a botanical bookmark. |
| `ic_success_spark` | `app/src/main/res/drawable/ic_success_spark.xml` | Micro-Graphic | Apache 2.0 (Original) | 4-point sparkle micro-graphic used during medication completion confirmation. |

---

## 💫 Motion Design & Micro-Interactions

| Interaction Pattern | Implementation Location | Animation Behavior | Accessibility / Performance |
| :--- | :--- | :--- | :--- |
| **Responsive Button Press Feedback** | `app/src/main/java/com/havn/app/ui/components/HavnMotion.kt` (`havnPressFeedback`) | Spring-based scale bounce (`0.92f`–`0.98f`) on press with quick release. | Bypasses scale animation when system `ANIMATOR_DURATION_SCALE` is set to 0. |
| **Skeleton Shimmer Loader** | `app/src/main/java/com/havn/app/ui/components/HavnMotion.kt` (`shimmerLoading`) | Warm linear gradient shimmer sliding across surface low/mid tokens. | Falls back to static surface tint if reduced motion is enabled. |
| **Staggered Content Entrance** | `app/src/main/java/com/havn/app/ui/components/HavnMotion.kt` (`StaggeredFadeIn`) | Fade-in with 20dp spring-assisted vertical entrance per list item (40ms stagger). | Renders content immediately without translation if reduced motion is enabled. |
| **Medication Completion Micro-Spark** | `app/src/main/java/com/havn/app/ui/screens/home/HomeScreen.kt` | Spring checkmark scale + subtle spark fade/scale entrance upon toggling dose to taken. | Fast 100-200ms duration, non-blocking, interruptible. |
| **Screen Transitions** | `app/src/main/java/com/havn/app/ui/navigation/HavnNavGraph.kt` | Fast horizontal slide + fade-in (stiffness 400f) for main routes; subtle vertical slide for modal views. | Performant Compose Nav transitions without heavy re-renders. |

---

## ♿ Accessibility & Reduced Motion

Hävn respects Android's system-level animator scale (`Settings.Global.ANIMATOR_DURATION_SCALE`). When the user enables reduced motion or disables animations in device accessibility settings:

1. Dynamic scaling and translations automatically resolve to 1.0f / 0dp.
2. Shimmer effects fall back to clean static surface tones.
3. Content reveals without delays or spatial motion.
