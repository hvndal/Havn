#!/usr/bin/env bash
set -euo pipefail

# Signs Havn-2.0.1-31-unsigned.aab with your original Google Play upload keystore.
# Expected upload key SHA1 (from 30.aab):
#   70:D6:F0:8C:F6:B4:6E:49:E3:A5:4E:4F:D7:ED:C0:06:6A:54:DE:4C
#   Owner: CN=Herman Hundal, O=Mander, L=Mohali, C=IN

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYSTORE="${1:-${HAVN_KEYSTORE:-$SCRIPT_DIR/../havn-upload.jks}}"
ALIAS="${2:-${HAVN_KEY_ALIAS:-havn}}"
UNSIGNED_AAB="$SCRIPT_DIR/Havn-2.0.1-31-unsigned.aab"
OUTPUT_AAB="$SCRIPT_DIR/Havn-2.0.1-31-play-upload.aab"

if [[ ! -f "$KEYSTORE" ]]; then
  echo "Usage: $0 /path/to/havn-upload.jks [alias]"
  echo "Expected upload key SHA1: 70:D6:F0:8C:F6:B4:6E:49:E3:A5:4E:4F:D7:ED:C0:06:6A:54:DE:4C"
  exit 1
fi

cp "$UNSIGNED_AAB" "$OUTPUT_AAB"
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore "$KEYSTORE" "$OUTPUT_AAB" "$ALIAS"

echo ""
echo "Signed Play Store upload bundle ready at:"
echo "  $OUTPUT_AAB"
