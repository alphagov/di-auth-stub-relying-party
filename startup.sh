#!/usr/bin/env bash
set -eu

./gradlew :spotlessApply

echo "Building DI authentication Stub relying party"
./gradlew clean build

export $(grep -v '^#' .env | xargs)

echo "Starting DI authentication Stub relying party"
./gradlew run