#!/usr/bin/env sh
# Lightweight Gradle bootstrapper for MusicMe CI/local builds.
set -eu
ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=9.3.1
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/gradle-${GRADLE_VERSION}-bin/musicme"
GRADLE_HOME="$DIST_DIR/gradle-${GRADLE_VERSION}"
if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$DIST_DIR"
  ZIP="$DIST_DIR/gradle-${GRADLE_VERSION}-bin.zip"
  if [ ! -f "$ZIP" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget -q "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -O "$ZIP"
    else
      echo "curl or wget is required to download Gradle." >&2
      exit 1
    fi
  fi
  command -v unzip >/dev/null 2>&1 || { echo "unzip is required." >&2; exit 1; }
  unzip -q -o "$ZIP" -d "$DIST_DIR"
fi
exec "$GRADLE_HOME/bin/gradle" -p "$ROOT_DIR" "$@"
