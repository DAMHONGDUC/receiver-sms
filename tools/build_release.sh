#!/usr/bin/env bash
# Shared by build_release_apk.sh / build_release_aab.sh: ./build_release.sh <apk|aab>.
# FLAVOR=dev builds the dev flavor (default prod); the version comes from env/version.properties.
set -euo pipefail

format="${1:-}"
flavor="${FLAVOR:-prod}"
root="$(cd "$(dirname "$0")/.." && pwd)"

case "$format" in
  apk) format_title="Apk" ;;
  aab) format_title="Aab" ;;
  *) echo "usage: $0 <apk|aab>" >&2; exit 1 ;;
esac
case "$flavor" in
  dev|prod) ;;
  *) echo "FLAVOR must be dev or prod, got '$flavor'" >&2; exit 1 ;;
esac
if [[ ! -f "$root/env/version.properties" ]]; then
  echo "missing env/version.properties" >&2; exit 1
fi

flavor_title="$(tr '[:lower:]' '[:upper:]' <<< "${flavor:0:1}")${flavor:1}"
"$root/gradlew" -p "$root" ":app:export${flavor_title}Release${format_title}"

echo "Release/:"
ls -1t "$root/Release" | grep "\.$format\$" | head -n 1
