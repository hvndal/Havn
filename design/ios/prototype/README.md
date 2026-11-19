# Hävn iOS prototype

A browser prototype of the whole iPhone app: the motion, sounds and every screen and state in `../SPEC.md`. It is the reference for timing and feel, not production code.

Open `havn.html` in a browser. It starts at Welcome, like a first launch.

- **Buttons above the phone:** jump to the app, the Home Screen widgets or the Lock Screen.
- **Panel below the phone:** largest text, Reduce Transparency, Reduce Motion, Increase Contrast, reminders off, no medications and the Face ID lock.
- **Right-hand column:** names every motion with its iOS term, each with a Play button.

## Files

| Path | What it is |
|---|---|
| `base.html` | Today with its motion, sounds and Liquid Glass. The other screens are built on top of it. |
| `src/app.css` | Styles for everything past Today |
| `src/10-core.js` | People, as-needed doses, alerts, the people menu |
| `src/20-onboarding.js` | Welcome, Health import, Restore from a backup, the ring flying into Today |
| `src/30-add.js` | Add Medication in six steps, with schedules |
| `src/40-history-meds.js` | History (week, month, day, logging a dose later) and Medications (list, detail, archive) |
| `src/50-settings-scenes.js` | Settings, backups, Face ID lock, Home Screen widgets, Lock Screen, the states panel |
| `src/60-notes-report.js` | Notes on doses and days, and the PDF report for your doctor |
| `src/90-init.js` | Wiring and the motion catalogue |
| `src/markup.html` | The sheets and scenes added inside the phone |
| `build.py` | Puts it all together: `python3 build.py` writes `havn.html` |

The build stops with an error if any part of `base.html` it changes isn't found exactly once, so edits fail loudly instead of quietly.

Fonts: in the browser it uses the system font; on a Mac or iPhone that is SF Pro. Apple's font files are not included and must not be added to this repo.
