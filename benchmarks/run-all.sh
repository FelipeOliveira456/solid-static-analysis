#!/usr/bin/env bash
# Run full pipeline for every immediate subfolder of benchmarks/ (each is a separate project).
set -euo pipefail
REPO="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO"
JAR="$REPO/target/solid-static-analysis.jar"
if [[ ! -f "$JAR" ]]; then
  echo "Build first: mvn -q package" >&2
  exit 1
fi
shopt -s nullglob
for d in "$REPO/benchmarks"/*/; do
  name="$(basename "$d")"
  echo "=== benchmarks/$name ==="
  java -jar "$JAR" --all "$(cd "$d" && pwd)"
done
echo "Done."
