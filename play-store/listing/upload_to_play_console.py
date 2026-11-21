#!/usr/bin/env python3
"""
Google Play Developer API v3 Automated Publisher for Hävn (com.havnmeds.app)

Uploads and commits:
1. All 87 localized store listings (title, shortDescription, fullDescription) from fastlane/metadata/android/<locale>/
2. Store graphics (512x512 icon, 1024x500 featureGraphic, phoneScreenshots, sevenInchScreenshots, tenInchScreenshots)
3. Optional: Release AAB (--aab), R8 deobfuscation mapping.txt (--mapping), native debug symbols zip (--symbols), and track release notes (--track internal|alpha|beta|production)

Usage:
  # 1) Update all 87 store listings + graphics only (no AAB required):
  python3 play-store/listing/upload_to_play_console.py \
      --service-account /path/to/play-service-account.json

  # 2) Update all 87 store listings + graphics AND upload signed v2.0.1 (versionCode 31) AAB + R8 mapping:
  python3 play-store/listing/upload_to_play_console.py \
      --service-account /path/to/play-service-account.json \
      --aab release-output/Havn-2.0.1-31-play-upload.aab \
      --mapping release-output/mapping-2.0.1-31.txt \
      --symbols release-output/native-debug-symbols-2.0.1-31.zip \
      --track production
"""

import argparse
import glob
import os
import sys
from pathlib import Path

PACKAGE_NAME = "com.havnmeds.app"
REPO_ROOT = Path(__file__).resolve().parents[2]
FASTLANE_DIR = REPO_ROOT / "fastlane" / "metadata" / "android"
GRAPHICS_DIR = REPO_ROOT / "play-store" / "graphics"


def load_text(path: Path) -> str:
    return path.read_text(encoding="utf-8").strip()


def build_service(service_account_json: Path):
    try:
        from google.oauth2 import service_account
        from googleapiclient.discovery import build
    except ImportError:
        print(
            "Missing google-api-python-client / google-auth.\n"
            "Install with: pip install google-api-python-client google-auth",
            file=sys.stderr,
        )
        sys.exit(1)

    credentials = service_account.Credentials.from_service_account_file(
        str(service_account_json),
        scopes=["https://www.googleapis.com/auth/androidpublisher"],
    )
    return build("androidpublisher", "v3", credentials=credentials, cache_discovery=False)


def sync_listings(edits, edit_id: str, only_locales=None) -> int:
    locale_dirs = sorted(p for p in FASTLANE_DIR.iterdir() if p.is_dir())
    updated = 0
    for loc_dir in locale_dirs:
        locale = loc_dir.name
        if only_locales and locale not in only_locales:
            continue
        title_file = loc_dir / "title.txt"
        short_file = loc_dir / "short_description.txt"
        full_file = loc_dir / "full_description.txt"
        if not (title_file.exists() and short_file.exists() and full_file.exists()):
            continue

        body = {
            "language": locale,
            "title": load_text(title_file),
            "shortDescription": load_text(short_file),
            "fullDescription": load_text(full_file),
        }
        edits.listings().update(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            language=locale,
            body=body,
        ).execute()
        updated += 1
        print(f"  [listing] {locale:10s} -> {body['title']} ({len(body['title'])}/30)")
    return updated


def upload_images_for_type(edits, edit_id: str, locale: str, image_type: str, file_paths):
    from googleapiclient.http import MediaFileUpload

    valid_paths = [Path(p) for p in file_paths if Path(p).exists()]
    if not valid_paths:
        return
    edits.images().deleteall(
        packageName=PACKAGE_NAME,
        editId=edit_id,
        language=locale,
        imageType=image_type,
    ).execute()
    for img_path in valid_paths:
        media = MediaFileUpload(str(img_path), mimetype="image/png", resumable=False)
        edits.images().upload(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            language=locale,
            imageType=image_type,
            media_body=media,
        ).execute()
        print(f"  [image]   {locale} / {image_type:22s} <- {img_path.name}")


def sync_graphics(edits, edit_id: str, locale: str = "en-US"):
    upload_images_for_type(
        edits, edit_id, locale, "icon", [GRAPHICS_DIR / "icon-512.png"]
    )
    upload_images_for_type(
        edits, edit_id, locale, "featureGraphic", [GRAPHICS_DIR / "feature-graphic-1024x500.png"]
    )
    phone_shots = sorted((GRAPHICS_DIR / "screenshots" / "phone").glob("*.png"))
    tablet7_shots = sorted((GRAPHICS_DIR / "screenshots" / "tablet-7").glob("*.png"))
    tablet10_shots = sorted((GRAPHICS_DIR / "screenshots" / "tablet-10").glob("*.png"))

    upload_images_for_type(edits, edit_id, locale, "phoneScreenshots", phone_shots[:8])
    upload_images_for_type(edits, edit_id, locale, "sevenInchScreenshots", tablet7_shots[:8])
    upload_images_for_type(edits, edit_id, locale, "tenInchScreenshots", tablet10_shots[:8])


def upload_bundle_and_track(
    edits,
    edit_id: str,
    aab_path: Path,
    mapping_path: Path = None,
    symbols_path: Path = None,
    track: str = "internal",
    release_status: str = "completed",
):
    from googleapiclient.http import MediaFileUpload

    print(f"  [bundle]  Uploading {aab_path.name} ...")
    media = MediaFileUpload(str(aab_path), mimetype="application/octet-stream", resumable=True)
    bundle_res = edits.bundles().upload(
        packageName=PACKAGE_NAME,
        editId=edit_id,
        media_body=media,
    ).execute()
    version_code = bundle_res["versionCode"]
    print(f"  [bundle]  Uploaded versionCode = {version_code}")

    if mapping_path and mapping_path.exists():
        print(f"  [mapping] Uploading R8 mapping {mapping_path.name} ...")
        map_media = MediaFileUpload(str(mapping_path), mimetype="application/octet-stream", resumable=True)
        edits.deobfuscationfiles().upload(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            apkVersionCode=version_code,
            deobfuscationFileType="proguard",
            media_body=map_media,
        ).execute()

    if symbols_path and symbols_path.exists():
        print(f"  [symbols] Uploading native debug symbols {symbols_path.name} ...")
        sym_media = MediaFileUpload(str(symbols_path), mimetype="application/octet-stream", resumable=True)
        edits.deobfuscationfiles().upload(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            apkVersionCode=version_code,
            deobfuscationFileType="nativeCode",
            media_body=sym_media,
        ).execute()

    changelog_file = FASTLANE_DIR / "en-US" / "changelogs" / f"{version_code}.txt"
    if not changelog_file.exists():
        changelog_file = FASTLANE_DIR / "en-US" / "changelogs" / "default.txt"
    release_notes = []
    if changelog_file.exists():
        release_notes.append({"language": "en-US", "text": load_text(changelog_file)})

    track_body = {
        "track": track,
        "releases": [
            {
                "name": f"{version_code} (2.0.1)",
                "versionCodes": [str(version_code)],
                "status": release_status,
                "releaseNotes": release_notes,
            }
        ],
    }
    edits.tracks().update(
        packageName=PACKAGE_NAME,
        editId=edit_id,
        track=track,
        body=track_body,
    ).execute()
    print(f"  [track]   Assigned versionCode {version_code} to track '{track}' ({release_status})")


def main():
    parser = argparse.ArgumentParser(description="Publish Hävn store listings, graphics, and AAB to Google Play Console.")
    parser.add_argument(
        "--service-account",
        default=os.environ.get("PLAY_SERVICE_ACCOUNT_JSON"),
        help="Path to Google Play Service Account JSON key (or set PLAY_SERVICE_ACCOUNT_JSON).",
    )
    parser.add_argument("--aab", help="Optional path to signed .aab file to upload.")
    parser.add_argument("--mapping", help="Optional path to R8 mapping.txt file.")
    parser.add_argument("--symbols", help="Optional path to native-debug-symbols.zip file.")
    parser.add_argument(
        "--track",
        default="internal",
        choices=["internal", "alpha", "beta", "production"],
        help="Release track when --aab is provided (default: internal).",
    )
    parser.add_argument(
        "--status",
        default="completed",
        choices=["completed", "draft"],
        help="Release status in Play Console (default: completed; use 'draft' if app is not yet published).",
    )
    parser.add_argument("--skip-graphics", action="store_true", help="Skip uploading icon, feature graphic, and screenshots.")
    parser.add_argument("--skip-listings", action="store_true", help="Skip uploading the 87 localized text listings.")
    args = parser.parse_args()

    if not args.service_account or not Path(args.service_account).exists():
        print(
            "Error: Provide a Google Play Service Account JSON key via --service-account /path/to/key.json\n"
            "or export PLAY_SERVICE_ACCOUNT_JSON=/path/to/key.json",
            file=sys.stderr,
        )
        sys.exit(1)

    service = build_service(Path(args.service_account))
    edits = service.edits()
    edit = edits.insert(body={}, packageName=PACKAGE_NAME).execute()
    edit_id = edit["id"]
    print(f"Created Play Console edit session: {edit_id} for {PACKAGE_NAME}")

    if not args.skip_listings:
        count = sync_listings(edits, edit_id)
        print(f"Updated {count} localized store listings.")

    if not args.skip_graphics:
        sync_graphics(edits, edit_id, locale="en-US")

    if args.aab:
        upload_bundle_and_track(
            edits,
            edit_id,
            Path(args.aab),
            Path(args.mapping) if args.mapping else None,
            Path(args.symbols) if args.symbols else None,
            track=args.track,
            release_status=args.status,
        )

    commit_res = edits.commit(packageName=PACKAGE_NAME, editId=edit_id).execute()
    print(f"Successfully committed Play Console edit {commit_res['id']}!")


if __name__ == "__main__":
    main()
