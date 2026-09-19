#!/usr/bin/env bash
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

PIDS_FILE="$ROOT_DIR/logs/pids.txt"

if [ -f "$PIDS_FILE" ]; then
  echo "==> Stopping the 4 Spring Boot services..."
  while read -r pid; do
    kill "$pid" 2>/dev/null || true
  done < "$PIDS_FILE"
  rm -f "$PIDS_FILE"
else
  echo "==> No logs/pids.txt found, skipping service shutdown."
fi

echo "==> Stopping infrastructure (MySQL, Kafka, Zookeeper)..."
docker-compose down

echo "Done."
