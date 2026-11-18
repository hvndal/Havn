# Hävn for iOS

The iPhone version of Hävn: free, offline and private. There is no Xcode project here yet; this folder holds what the project will need.

| Path | What it is |
|---|---|
| `APP_STORE_GUIDE.md` | **Start here.** Apple's requirements, the private-build decisions, every App Store Connect answer, the steps to ship, and the decisions still open |
| `PRIVACY.md` | iOS privacy policy, to publish at the privacy URL before release |
| `Havn/PrivacyInfo.xcprivacy` | Privacy manifest: no tracking, no data collected |
| `Havn/Localizable.xcstrings` | In-app text in English plus 10 languages |
| `Havn/InfoPlist.xcstrings` | App name and permission prompts in the same languages |
| `fastlane/metadata/<locale>/` | App Store product page text for 14 store locales |
| `tools/build_localization.py` | The one place the text and translations live for now; writes the two catalogs and the product pages |
| `tools/check.py` | Checks Apple's length limits, keyword bytes, missing languages and placeholders. Exits with an error if anything fails |

Design: `../design/ios/BRIEF.md`.

## Change the text

```
python3 ios/tools/build_localization.py   # rewrite catalogs and product pages
python3 ios/tools/check.py                # must say 0 errors
```

Every translation is marked "needs review" until a native speaker has read it in the running app.
