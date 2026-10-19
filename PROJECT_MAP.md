# Project map — where everything is

Start here. Every folder in this repo, what it's for, and whether it's current.

## The app (Android)
| Path | What |
|---|---|
| `app/src/main/java/com/havn/app/` | All Kotlin code |
| `…/ui/screens/` | One folder per screen: `home` (Today), `addmed`, `managemeds`, `progress` (History), `reminders`, `settings`, `onboarding`, `organizer` (3D) |
| `…/ui/components/` | Shared UI pieces (buttons, dose check, text fields…) |
| `…/ui/theme/` | Colours, fonts, motion, spacing. **The visual overhaul starts here** |
| `…/notifications/` | Reminders, alarms, reboot handling |
| `…/data/` | Database, backup, preferences |
| `…/widget/` | Home-screen widget |
| `app/src/main/res/` | Fonts, sounds (`raw/`), icons, strings |
| `app/src/main/assets/licenses/` | Font licences (OFL) shipped with the app |
| `build-release.sh`, `gradlew` | Build the release |

## Design — current (Swiss)
| Path | What |
|---|---|
| `design/swiss/PLAN.md` | **2-week plan, daily approvals, your homework** |
| `design/swiss/VISUAL_CHECKLIST.md` | Everything the final app should look, move and sound like |
| `design/swiss/IDEAS.md` | Approved / unreviewed / dropped ideas |
| `design/swiss/preview.html` | 8 static screens |
| `design/swiss/onboarding.html` | Live onboarding with motion |
| `design/swiss/FIELD_NOTES.md` | The 55 daily lines |
| `design/swiss/HANDOFF.md` | Original Swiss handoff (tokens, rules) |
| `design/swiss/TASKS.md` | Implementation prompt (older; PLAN.md supersedes the order) |
| Figma | https://www.figma.com/design/ZVdpAZLnyIfsAcsKzmIDAJ — same screens as the two HTML previews |

## Design — archive (not current)
| Path | What |
|---|---|
| `design/archive/cinematheque/` | The earlier glass-capsule "Cinematheque" direction: design file, shaders, pill/capsule assets, export tool. Kept for reference only |

## Store and marketing
| Path | What |
|---|---|
| `play-store/listing/` | Store text, privacy policy, Play Console answers |
| `play-store/graphics/` | Icon, feature graphic, screenshots (old look; redo on Day 13) |
| `play-store/screenshot-builder/` | HTML used to make the old screenshots |
| `play-store/reels/` | Promo videos (old look) |
| `design/email/closed-testing.html` | Tester invite email |

## Other
| Path | What |
|---|---|
| `web-prototype/` | Early React/Vite web prototype. Not part of the Android app; not current |
| `README.md` | What the app does |
| `START-HERE.md`, `PUBLISHING.md` | How to build and publish |
| `PRIVACY.md` | Privacy statement |
| `AGENTS.md`, `GEMINI.md` | Instructions for AI tools (to be refreshed at the end of the overhaul) |
