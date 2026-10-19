#!/bin/sh
set -e

cd "$(dirname "$0")"

KTS=target_teavm_wasm_gc/build.gradle.kts
SRC=desktopRuntime/resources
STAGE=target_teavm_wasm_gc/epk_src

[ -f "$KTS.bak" ] || cp "$KTS" "$KTS.bak"
sed -i 's|epkSources = file("../desktopRuntime/resources")|epkSources = file("epk_src")|' "$KTS"

rm -rf "$STAGE"
mkdir -p "$STAGE"
cp -a "$SRC"/. "$STAGE"/
rm -f "$STAGE/assets.epk"

chmod +x gradlew
./gradlew target_teavm_wasm_gc:makeMainWasmClientBundle -Penv=prod

ls -lh target_teavm_wasm_gc/javascript/assets.epk target_teavm_wasm_gc/javascript_dist/*.html
