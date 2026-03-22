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
  out="$REPO/output/$name"
  if [[ -d "$out" ]]; then
    # Legacy Etapa 1 JSON at project root; current layout uses ast/*.json only
    find "$out" -maxdepth 1 -type f -name '*.json' -delete 2>/dev/null || true
  fi
  java -jar "$JAR" --all "$(cd "$d" && pwd)"
done
echo "Done."
