#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
#  Hävn — one-command Play Store build
#
#    bash build-release.sh
#
#  Works in GitHub Codespaces, Linux and macOS. It will:
#    1. install the Android SDK if missing (≈1 GB, first run only)
#    2. create your upload key on the first run (asks for a password)
#    3. build the signed .aab Google Play needs
#  Output: release-output/
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail
cd "$(dirname "$0")"
ROOT="$PWD"
OUT="$ROOT/release-output"
KEYDIR="$OUT/KEEP-THIS-SAFE"
say()  { printf '\n\033[1;32m▶ %s\033[0m\n' "$*"; }
fail() { printf '\n\033[1;31m✖ %s\033[0m\n' "$*"; exit 1; }

# ── 1. Java 17+ ──────────────────────────────────────────────────────────────
java_major() { java -version 2>&1 | awk -F'"' '/version/{split($2,v,".");print (v[1]=="1")?v[2]:v[1]}'; }
if ! command -v java >/dev/null || [ "$(java_major)" -lt 17 ]; then
  if command -v apt-get >/dev/null; then
    say "Installing Java 17"
    sudo apt-get update -qq && sudo apt-get install -y -qq openjdk-17-jdk-headless unzip
  elif command -v brew >/dev/null; then
    say "Installing Java 17"
    brew install --quiet openjdk@17
    export JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
    export PATH="$JAVA_HOME/bin:$PATH"
  else
    fail "Install Java 17 or newer (https://adoptium.net) and run again."
  fi
fi
say "Java $(java_major) OK"

# ── 2. Android SDK ───────────────────────────────────────────────────────────
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/android-sdk}}"
SDKMANAGER="$SDK/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "$SDKMANAGER" ]; then
  say "Installing Android command-line tools into $SDK"
  case "$(uname -s)" in
    Linux)  OS=linux ;;
    Darwin) OS=mac ;;
    *) fail "Unsupported OS. Use GitHub Codespaces or WSL." ;;
  esac
  mkdir -p "$SDK/cmdline-tools"
  TMP="$(mktemp -d)"
  ok=""
  for BUILD in 14742923 13114758 11076708; do
    if curl -fsSL -o "$TMP/tools.zip" \
      "https://dl.google.com/android/repository/commandlinetools-${OS}-${BUILD}_latest.zip"; then
      ok=1; break
    fi
  done
  [ -n "$ok" ] || fail "Couldn't download Android command-line tools."
  unzip -q "$TMP/tools.zip" -d "$TMP"
  rm -rf "$SDK/cmdline-tools/latest"
  mv "$TMP/cmdline-tools" "$SDK/cmdline-tools/latest"
  rm -rf "$TMP"
fi
export ANDROID_HOME="$SDK" ANDROID_SDK_ROOT="$SDK"
say "Accepting SDK licences and installing platform 36"
yes | "$SDKMANAGER" --sdk_root="$SDK" --licenses >/dev/null 2>&1 || true
"$SDKMANAGER" --sdk_root="$SDK" "platforms;android-36" "build-tools;36.0.0" "platform-tools" >/dev/null
echo "sdk.dir=$SDK" > local.properties

# ── 3. Upload key (first run only) ───────────────────────────────────────────
if [ ! -f keystore.properties ]; then
  mkdir -p "$KEYDIR"
  KS="$KEYDIR/havn-upload.jks"
  if [ -f "$KS" ]; then
    fail "$KS exists but keystore.properties is missing. Recreate keystore.properties (see keystore.properties.example)."
  fi
  say "Creating your upload key — pick a password (min 6 chars) and SAVE IT"
  while :; do
    read -rsp "  Key password: " PW; echo
    read -rsp "  Repeat it:    " PW2; echo
    [ "$PW" = "$PW2" ] && [ "${#PW}" -ge 6 ] && break
    echo "  Didn't match or too short, try again."
  done
  keytool -genkeypair -keystore "$KS" -storetype PKCS12 -alias havn \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass "$PW" -keypass "$PW" \
    -dname "CN=Herman Hundal, O=Mander, L=Mohali, C=IN" >/dev/null
  cat > keystore.properties <<PROPS
storeFile=$KS
storePassword=$PW
keyAlias=havn
keyPassword=$PW
PROPS
  chmod 600 keystore.properties "$KS"
  cat > "$KEYDIR/README.txt" <<TXT
havn-upload.jks = your Google Play upload key (alias: havn).
Download it and store it + its password somewhere safe (password manager,
private drive). Every future Hävn update must be signed with this key.
Never commit it to GitHub or share it.
TXT
  unset PW PW2
fi

# ── 4. Build ─────────────────────────────────────────────────────────────────
say "Building signed release bundle (first run takes a few minutes)"
chmod +x gradlew
./gradlew --no-daemon clean bundleRelease

AAB="app/build/outputs/bundle/release/app-release.aab"
[ -f "$AAB" ] || fail "Build finished but no .aab was found."
VERSION="$(grep -m1 'versionName' app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')"
mkdir -p "$OUT"
cp "$AAB" "$OUT/Havn-$VERSION.aab"
cp app/build/outputs/mapping/release/mapping.txt "$OUT/mapping-$VERSION.txt" 2>/dev/null || true

say "Done ✅"
echo "  Upload to Play Console:  release-output/Havn-$VERSION.aab"
echo "  Deobfuscation file:      release-output/mapping-$VERSION.txt (optional upload)"
[ -d "$KEYDIR" ] && echo "  BACK UP YOUR KEY:        release-output/KEEP-THIS-SAFE/havn-upload.jks"
echo
echo "  Codespaces: right-click the release-output folder → Download."
