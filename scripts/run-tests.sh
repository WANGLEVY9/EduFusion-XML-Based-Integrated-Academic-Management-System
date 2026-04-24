#!/usr/bin/env bash
set -euo pipefail

echo "Running unit and service tests..."
mvn -B clean test
echo "Tests passed."
