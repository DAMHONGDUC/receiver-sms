#!/usr/bin/env bash
# Builds the signed release AAB and copies it into Release/. FLAVOR=dev builds the dev flavor instead of prod.
set -euo pipefail
exec "$(dirname "$0")/build_release.sh" aab "$@"
