#!/usr/bin/env bash
set -eu

echo "Building DI authentication Stub relying party"
./gradlew clean build

echo "Starting DI authentication Stub relying party"
./gradlew run