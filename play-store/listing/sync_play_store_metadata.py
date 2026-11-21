#!/usr/bin/env python3
"""
Validate and sync all 87 Google Play Console localized store listings into
Fastlane Supply format (`fastlane/metadata/android/<locale>/`).
"""

from __future__ import annotations

import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
TRANSLATIONS_DIR = pathlib.Path(__file__).resolve().parent / "translations"
OUT_DIR = ROOT / "fastlane" / "metadata" / "android"

# Official Google Play Console supported store-listing locales (87 total)
# Source: https://support.google.com/googleplay/android-developer/answer/9844778
PLAY_STORE_LOCALES = {
    "af", "am", "ar", "az-AZ", "be", "bg", "bn-BD", "ca", "cs-CZ", "da-DK",
    "de-DE", "el-GR", "en-AU", "en-CA", "en-GB", "en-IN", "en-SG", "en-US",
    "en-ZA", "es-419", "es-ES", "es-US", "et", "eu-ES", "fa", "fa-AE",
    "fa-AF", "fa-IR", "fi-FI", "fil", "fr-CA", "fr-FR", "gl-ES", "gu",
    "hi-IN", "hr", "hu-HU", "hy-AM", "id", "is-IS", "it-IT", "iw-IL", "ja-JP",
    "ka-GE", "kk", "km-KH", "kn-IN", "ko-KR", "ky-KG", "lo-LA", "lt", "lv",
    "mk-MK", "ml-IN", "mn-MN", "mr-IN", "ms", "ms-MY", "my-MM", "ne-NP",
    "nl-NL", "no-NO", "pa", "pl-PL", "pt-BR", "pt-PT", "rm", "ro", "ru-RU",
    "si-LK", "sk", "sl", "sq", "sr", "sv-SE", "sw", "ta-IN", "te-IN", "th",
    "tr-TR", "uk", "ur", "vi", "zh-CN", "zh-HK", "zh-TW", "zu",
}

LIMITS = {
    "title": 30,
    "short_description": 80,
    "full_description": 4000,
}


def utf16_len(text: str) -> int:
    return len(text.encode("utf-16-le")) // 2


def main() -> int:
    check_only = "--check" in sys.argv
    entries: dict[str, dict[str, str]] = {}
    duplicates: list[str] = []

    for path in sorted(TRANSLATIONS_DIR.glob("batch*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        for locale, payload in data.items():
            if locale in entries:
                duplicates.append(locale)
            entries[locale] = payload

    errors: list[str] = []
    if duplicates:
        errors.append(f"Duplicate locales found: {sorted(duplicates)}")

    missing = sorted(PLAY_STORE_LOCALES - set(entries.keys()))
    extra = sorted(set(entries.keys()) - PLAY_STORE_LOCALES)
    if missing:
        errors.append(f"Missing Play Store locales ({len(missing)}): {missing}")
    if extra:
        errors.append(f"Unsupported locales ({len(extra)}): {extra}")

    for locale in sorted(entries):
        item = entries[locale]
        for field, max_len in LIMITS.items():
            value = item.get(field, "").strip()
            if not value:
                errors.append(f"[{locale}] {field} is empty")
                continue
            if field in ("title", "short_description") and "\n" in value:
                errors.append(f"[{locale}] {field} must be single-line")
            rune_len = len(value)
            u16_len = utf16_len(value)
            if max(rune_len, u16_len) > max_len:
                errors.append(
                    f"[{locale}] {field} exceeds {max_len} chars (got {rune_len} runes / {u16_len} utf16): {value!r}"
                )

        title = item.get("title", "")
        full = item.get("full_description", "")
        if "Hävn" not in title:
            errors.append(f"[{locale}] title missing brand name 'Hävn'")
        if "Hävn" not in full or "mander.tech" not in full:
            errors.append(f"[{locale}] full_description missing 'Hävn' or 'mander.tech'")
        bullets = [ln for ln in full.splitlines() if ln.startswith("• ")]
        if len(bullets) != 8:
            errors.append(f"[{locale}] expected 8 bullet lines, found {len(bullets)}")

    if errors:
        print(f"Validation FAILED with {len(errors)} error(s):", file=sys.stderr)
        for err in errors:
            print(f"  - {err}", file=sys.stderr)
        return 1

    if not check_only:
        OUT_DIR.mkdir(parents=True, exist_ok=True)
        for existing in list(OUT_DIR.iterdir()):
            if existing.is_dir() and existing.name not in PLAY_STORE_LOCALES:
                print(f"Warning: unexpected directory in {OUT_DIR}: {existing.name}", file=sys.stderr)
        for locale, item in sorted(entries.items()):
            loc_dir = OUT_DIR / locale
            loc_dir.mkdir(parents=True, exist_ok=True)
            for field in ("title", "short_description", "full_description"):
                (loc_dir / f"{field}.txt").write_text(
                    item[field].strip() + "\n",
                    encoding="utf-8",
                )

    max_title = max(len(v["title"].strip()) for v in entries.values())
    max_short = max(len(v["short_description"].strip()) for v in entries.values())
    max_full = max(len(v["full_description"].strip()) for v in entries.values())
    action = "Checked" if check_only else "Generated & verified"
    print(
        f"{action} {len(entries)}/{len(PLAY_STORE_LOCALES)} Google Play Console locales "
        f"(max title={max_title}/30, max short={max_short}/80, max full={max_full}/4000)."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
