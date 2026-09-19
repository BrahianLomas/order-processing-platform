#!/usr/bin/env bash
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

LOG_DIR="$ROOT_DIR/logs"
mkdir -p "$LOG_DIR"
rm -f "$LOG_DIR/pids.txt"

wait_for_port() {
  local host=$1 port=$2 name=$3
  echo -n "==> Waiting for $name ($host:$port)..."
  for _ in $(seq 1 60); do
    if (echo > "/dev/tcp/$host/$port") >/dev/null 2>&1; then
      echo " ready"
      return 0
    fi
    sleep 2
  done
  echo " timeout after 120s"
  return 1
}

wait_for_container_healthy() {
  local container=$1
  echo -n "==> Waiting for $container to be healthy..."
  for _ in $(seq 1 30); do
    status=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "starting")
    if [ "$status" = "healthy" ]; then
      echo " ready"
      return 0
    fi
    sleep 3
  done
  echo " timeout after 90s"
  return 1
}

open_url() {
  local url=$1
  case "$OSTYPE" in
    msys*|cygwin*) start "" "$url" ;;
    darwin*) open "$url" ;;
    *) xdg-open "$url" >/dev/null 2>&1 ;;
  esac
}

echo "==> Starting infrastructure (MySQL, Kafka, Zookeeper)..."
docker-compose up -d

wait_for_container_healthy order-platform-mysql
wait_for_port localhost 9092 "Kafka"

echo "==> Compiling all modules (build/check/tests are CI's job, not this script's)..."
./gradlew assemble --no-daemon --console=plain

start_service() {
  local module=$1 label=$2
  echo "==> Starting $label..."
  nohup ./gradlew ":$module:bootRun" --no-daemon --console=plain > "$LOG_DIR/$module.log" 2>&1 &
  echo $! >> "$LOG_DIR/pids.txt"
}

start_service api-gateway "API Gateway"
start_service order-service "Order Service"
start_service payment-service "Payment Service"
start_service notification-service "Notification Service"

wait_for_port localhost 8080 "API Gateway"
wait_for_port localhost 8081 "Order Service"
wait_for_port localhost 8082 "Payment Service"

echo "==> All services are up. Opening Swagger UIs..."
open_url "http://localhost:8080/api/swagger-ui/index.html"
open_url "http://localhost:8081/api/swagger-ui/index.html"
open_url "http://localhost:8082/api/swagger-ui/index.html"

echo "==> Opening Postman..."
case "$OSTYPE" in
  msys*|cygwin*)
    if [ -f "$LOCALAPPDATA/Postman/Postman.exe" ]; then
      "$LOCALAPPDATA/Postman/Postman.exe" &
    else
      echo "    Postman.exe not found under %LOCALAPPDATA%\\Postman — open it manually."
    fi
    ;;
  darwin*) open -a Postman >/dev/null 2>&1 || true ;;
  *) command -v postman >/dev/null 2>&1 && (postman & disown) || true ;;
esac
echo "    Collection: docs/assets/postman/order-processing-platform.postman_collection.json"
echo "    (first time: drag it into Postman to import — it's remembered after that)"

echo ""
echo "Ready. Logs: $LOG_DIR/*.log"
echo "To stop everything: ./stop.sh"
