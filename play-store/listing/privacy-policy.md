# Hävn Medication Reminder — Privacy Policy

_Last updated: November 21, 2026_

Hävn is made by Mander (https://mander.tech). This policy explains what the app does with your information.

## What we collect

Nothing. Hävn has 0 accounts, 0 servers, 0 analytics, 0 ads, and 0 tracking. Your medication data, profiles, and adherence logs stay strictly on your device in local SQLite storage and are never uploaded to us or any third party. (`INTERNET` and `BILLING` permissions are declared solely so Google Play Billing can handle an optional one-time Supporter purchase through Google Play Services.)

## What stays on your phone

The names, profiles, medications, schedules, day anchors, and dose history you enter are stored only in the app's private storage on your device (`Room` local database). Android cloud backup explicitly excludes Hävn's database and preferences via [`backup_rules.xml`](app/src/main/res/xml/backup_rules.xml) and [`data_extraction_rules.xml`](app/src/main/res/xml/data_extraction_rules.xml) (only local device-to-device migration is permitted).

## Backups and reports you export

If you export a `.havn` backup file (optionally encrypted with your own passphrase), a month summary PNG card, or an A4 Doctor PDF report, Hävn writes that file locally and hands it off through Android's system picker/share sheet to whichever destination you choose.

## In-app Supporter & external links

The optional one-time Supporter unlock (`havn_supporter`) is processed by Google Play Billing directly on Google Play. Any external `mander.tech` links open in your default browser under their own privacy policies.

## Deleting your data

Delete individual profiles, medicines, or all local data in Settings (`Delete everything`), or uninstall the app to remove all local storage immediately.

## Children

Hävn is not directed at children under 13.

## Medical disclaimer

Hävn is a reminder and recording tool. It does not provide medical advice or suggest doses. Always follow your doctor's or pharmacist's instructions.

## Contact

sales@mander.tech
