#!/bin/bash
set -euo pipefail

source_dir="$(cd "$(dirname "$0")" && pwd)"
java_home="${JAVA_HOME:-$(/usr/libexec/java_home)}"
clang -arch arm64 -arch x86_64 -mmacosx-version-min=11.0 \
    -fobjc-arc -dynamiclib -framework Cocoa -framework Carbon \
    -I"$java_home/include" -I"$java_home/include/darwin" \
    "$source_dir/PackageLauncher.m" -o "$source_dir/../gama-launcher.dylib"
