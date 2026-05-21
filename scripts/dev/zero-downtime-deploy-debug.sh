#!/bin/bash

set -e

APP_DIR="/home/ubuntu/app"
PORT_FILE="$APP_DIR/current_port.txt"
LOG_FILE="$APP_DIR/deploy.log"
BLUE_PORT=8080
GREEN_PORT=8081
BLUE_MONITOR_PORT=8083
GREEN_MONITOR_PORT=8084
MAX_HEALTH_CHECK_RETRIES=60
HEALTH_CHECK_INTERVAL=2
PROFILE="dev"
TIMEZONE="Asia/Seoul"

# 디버그 옵션: true면 기존 앱을 먼저 종료 후 새 앱 시작 (OOM 원인 분리용)
FORCE_STOP_OLD_FIRST="${FORCE_STOP_OLD_FIRST:-true}"

log() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "${timestamp} $@" | tee -a "$LOG_FILE"
}

error_exit() {
    log "$1"
    exit 1
}

diagnose_system() {
    log "========== SYSTEM DIAGNOSTICS =========="

    log "--- Memory ---"
    free -m 2>/dev/null | while read line; do log "  $line"; done

    log "--- /proc/meminfo (top 5) ---"
    head -5 /proc/meminfo 2>/dev/null | while read line; do log "  $line"; done

    log "--- Disk ---"
    df -h / 2>/dev/null | while read line; do log "  $line"; done

    log "--- Swap ---"
    sudo swapon --show 2>/dev/null | while read line; do log "  $line"; done
    if [ -z "$(sudo swapon --show 2>/dev/null)" ]; then
        log "  (no swap configured)"
    fi

    log "--- Top 10 memory consumers ---"
    ps aux --sort=-%mem 2>/dev/null | head -11 | while read line; do log "  $line"; done

    log "--- Running Java processes ---"
    ps aux 2>/dev/null | grep '[j]ava' | while read line; do log "  $line"; done
    if [ -z "$(ps aux 2>/dev/null | grep '[j]ava')" ]; then
        log "  (no java processes running)"
    fi

    log "--- CPU info ---"
    log "  CPUs: $(nproc 2>/dev/null || echo 'unknown')"

    log "========================================="
}

get_current_port() {
    if [ ! -f "$PORT_FILE" ]; then
        log "Port file not found. Initializing with default port $BLUE_PORT"
        echo "$BLUE_PORT" > "$PORT_FILE"
        echo "$BLUE_PORT"
    else
        cat "$PORT_FILE"
    fi
}

get_inactive_port() {
    local current_port=$1
    if [ "$current_port" -eq "$BLUE_PORT" ]; then
        echo "$GREEN_PORT"
    else
        echo "$BLUE_PORT"
    fi
}

get_monitor_port() {
    local app_port=$1
    if [ "$app_port" -eq "$BLUE_PORT" ]; then
        echo "$BLUE_MONITOR_PORT"
    else
        echo "$GREEN_MONITOR_PORT"
    fi
}

is_port_in_use() {
    local port=$1
    sudo lsof -t -i:$port > /dev/null 2>&1
    return $?
}

kill_process_on_port() {
    local port=$1
    local pid=$(sudo lsof -t -i:$port 2>/dev/null)

    if [ -z "$pid" ]; then
        log "No process running on port $port"
        return 0
    fi

    log "Sending graceful shutdown signal to process $pid on port $port"
    sudo kill -15 "$pid"

    local wait_count=0
    while [ $wait_count -lt 65 ] && is_port_in_use "$port"; do
        sleep 1
        wait_count=$((wait_count + 1))
    done

    if is_port_in_use "$port"; then
        log "Process didn't stop gracefully, forcing shutdown"
        sudo kill -9 "$pid" 2>/dev/null || true
        sleep 2
    fi

    log "Process on port $port stopped successfully"
}

dump_failure_diagnostics() {
    local port=$1

    log "========== FAILURE DIAGNOSTICS =========="

    log "--- Last 50 lines of app log (app-$port.log) ---"
    tail -50 "$APP_DIR/app-$port.log" 2>/dev/null | while read line; do log "  LOG: $line"; done

    log "--- System memory at failure ---"
    free -m 2>/dev/null | while read line; do log "  MEM: $line"; done

    log "--- dmesg OOM check ---"
    sudo dmesg 2>/dev/null | grep -i "oom\|killed\|out of memory" | tail -10 | while read line; do log "  DMESG: $line"; done
    if [ -z "$(sudo dmesg 2>/dev/null | grep -i 'oom\|killed\|out of memory')" ]; then
        log "  (no OOM entries found in dmesg)"
    fi

    log "--- JVM crash dump check ---"
    local crash_dumps=$(find /home/ubuntu -name 'hs_err_pid*.log' -newer "$APP_DIR/app-$port.jar" 2>/dev/null)
    if [ -n "$crash_dumps" ]; then
        echo "$crash_dumps" | while read f; do
            log "  CRASH DUMP FOUND: $f"
            head -30 "$f" 2>/dev/null | while read line; do log "    $line"; done
        done
    else
        log "  (no JVM crash dumps found)"
    fi

    log "--- Heap dump check ---"
    if [ -f "$APP_DIR/heapdump-$port.hprof" ]; then
        local dump_size=$(ls -lh "$APP_DIR/heapdump-$port.hprof" 2>/dev/null | awk '{print $5}')
        log "  HEAP DUMP FOUND: $APP_DIR/heapdump-$port.hprof (size: $dump_size)"
    else
        log "  (no heap dump found)"
    fi

    log "--- All java processes ---"
    ps aux 2>/dev/null | grep '[j]ava' | while read line; do log "  PS: $line"; done

    log "========================================="
}

health_check() {
    local port=$1
    local monitor_port=$2
    local health_url="http://localhost:$monitor_port/monitoring/health"

    log "Starting health check for port $port (monitor: $monitor_port)"
    log "Health check URL: $health_url"

    local retry=1
    while [ $retry -le $MAX_HEALTH_CHECK_RETRIES ]; do
        # Java 프로세스 생존 확인
        local java_pid=$(pgrep -f "app-$port.jar" 2>/dev/null)
        if [ -z "$java_pid" ]; then
            log "FATAL: Java process for port $port is no longer running!"
            dump_failure_diagnostics "$port"
            return 1
        fi

        local status=$(curl -s -o /dev/null -w "%{http_code}" "$health_url" 2>/dev/null || echo "000")

        log "Health check attempt $retry/$MAX_HEALTH_CHECK_RETRIES - Status: $status - PID: $java_pid"

        # 10회마다 메모리 상태 로깅
        if [ $((retry % 10)) -eq 1 ]; then
            local mem_info=$(free -m 2>/dev/null | grep Mem | awk '{print "Total:" $2 "MB Used:" $3 "MB Free:" $4 "MB Available:" $7 "MB"}')
            log "  Memory: $mem_info"
        fi

        if [ "$status" = "200" ]; then
            log "Health check passed!"
            return 0
        fi

        sleep $HEALTH_CHECK_INTERVAL
        retry=$((retry + 1))
    done

    # 헬스체크 최종 실패 - 전체 진단 덤프
    log "Health check failed after $MAX_HEALTH_CHECK_RETRIES attempts"
    dump_failure_diagnostics "$port"
    return 1
}

start_application() {
    local port=$1
    local monitor_port=$2
    local staging_jar="$APP_DIR/staging/app.jar"
    local jar_file="$APP_DIR/app-$port.jar"

    if [ ! -f "$staging_jar" ]; then
        error_exit "No JAR file found in staging directory: $staging_jar"
    fi

    log "Copying JAR from staging to $jar_file"
    cp "$staging_jar" "$jar_file"

    if is_port_in_use "$port"; then
        log "Port $port is in use, cleaning up..."
        kill_process_on_port "$port"
    fi

    local java_cmd="sudo nohup java \
        -XX:+HeapDumpOnOutOfMemoryError \
        -XX:HeapDumpPath=$APP_DIR/heapdump-$port.hprof \
        -Dspring.profiles.active=$PROFILE,monitor \
        -Duser.timezone=$TIMEZONE \
        -Dserver.port=$port \
        -Dmanagement.server.port=$monitor_port \
        -Ddd.service=debate-timer \
        -Ddd.env=$PROFILE \
        -jar $jar_file"

    log "Starting application on port $port with JAR: $jar_file"
    log "Executing: $java_cmd"

    sudo nohup java \
        -XX:+HeapDumpOnOutOfMemoryError \
        -XX:HeapDumpPath=$APP_DIR/heapdump-$port.hprof \
        -Dspring.profiles.active=$PROFILE,monitor \
        -Duser.timezone=$TIMEZONE \
        -Dserver.port=$port \
        -Dmanagement.server.port=$monitor_port \
        -Ddd.service=debate-timer \
        -Ddd.env=$PROFILE \
        -jar "$jar_file" > "$APP_DIR/app-$port.log" 2>&1 &

    sleep 3

    # pgrep으로 실제 Java PID 찾기 ($!는 sudo의 PID를 반환하므로 불정확)
    local java_pid=$(pgrep -f "app-$port.jar" 2>/dev/null)

    if [ -z "$java_pid" ]; then
        log "ERROR: Could not find java process after start"
        dump_failure_diagnostics "$port"
        error_exit "Application process not found after start"
    fi

    log "Found java process with PID: $java_pid"

    # 메모리 상태 확인
    local mem_info=$(free -m 2>/dev/null | grep Mem | awk '{print "Total:" $2 "MB Used:" $3 "MB Free:" $4 "MB Available:" $7 "MB"}')
    log "Memory after app start: $mem_info"
}

switch_nginx_upstream() {
    local new_port=$1
    local nginx_conf="/etc/nginx/sites-available/api.dev.debate-timer.com"
    local temp_conf="/tmp/api.dev.debate-timer.com.tmp"
    local backup_conf="${nginx_conf}.bak"

    if [ ! -f "$nginx_conf" ]; then
        error_exit "nginx configuration not found at $nginx_conf"
    fi

    log "Switching nginx upstream to port $new_port"
    sudo cp "$nginx_conf" "$backup_conf"

    sed "s/server 127\.0\.0\.1:[0-9]\+;/server 127.0.0.1:$new_port;/" "$nginx_conf" > "$temp_conf"
    sudo cp "$temp_conf" "$nginx_conf"

    if ! sudo nginx -t 2>/dev/null; then
        log "nginx configuration test failed, rolling back."
        sudo cp "$backup_conf" "$nginx_conf"
        sudo rm "$backup_conf"
        return 1
    fi

    sudo nginx -s reload
    log "nginx reloaded successfully"

    sleep 2
    local response=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost/" 2>/dev/null || echo "000")
    if [ "$response" = "000" ] || [ "$response" = "502" ] || [ "$response" = "503" ]; then
        log "nginx health check failed after reload (status: $response). Rolling back nginx config."
        sudo cp "$backup_conf" "$nginx_conf"
        sudo nginx -s reload
        sudo rm "$backup_conf"
        return 1
    fi

    log "nginx is now routing traffic to port $new_port"
    sudo rm "$backup_conf"
    return 0
}

main() {
    local current_port=$(get_current_port)
    local new_port=$(get_inactive_port "$current_port")
    local new_monitor_port=$(get_monitor_port "$new_port")

    log "Current active port: $current_port"
    log "Deploying to port: $new_port"
    log "Monitor port: $new_monitor_port"
    log "FORCE_STOP_OLD_FIRST: $FORCE_STOP_OLD_FIRST"

    log "=== PRE-DEPLOY DIAGNOSTICS ==="
    diagnose_system

    # OOM 원인 분리를 위해 기존 앱 먼저 종료
    if [ "$FORCE_STOP_OLD_FIRST" = "true" ]; then
        log "FORCE_STOP_OLD_FIRST is enabled. Killing old app on port $current_port first."
        kill_process_on_port "$current_port"
        local old_monitor_port=$(get_monitor_port "$current_port")
        kill_process_on_port "$old_monitor_port"
        sleep 2
        log "=== POST-CLEANUP MEMORY ==="
        free -m 2>/dev/null | while read line; do log "  $line"; done
    fi

    log "Step 1/4: Starting new version on port $new_port"
    start_application "$new_port" "$new_monitor_port"

    log "Step 2/4: Performing health check"
    if ! health_check "$new_port" "$new_monitor_port"; then
        log "Deployment failed: Health check did not pass"
        log "Rolling back: Stopping new version on port $new_port"
        kill_process_on_port "$new_port"
        error_exit "Deployment aborted due to health check failure"
    fi

    log "Step 3/4: Switching nginx to new version"
    if ! switch_nginx_upstream "$new_port"; then
        log "nginx switch failed, rolling back"
        kill_process_on_port "$new_port"
        error_exit "Deployment aborted due to nginx switch failure"
    fi

    log "Step 4/4: Stopping old version on port $current_port"
    if [ "$FORCE_STOP_OLD_FIRST" != "true" ]; then
        kill_process_on_port "$current_port"
    else
        log "Old version already stopped (FORCE_STOP_OLD_FIRST was enabled)"
    fi

    local old_jar="$APP_DIR/app-$current_port.jar"
    if [ -f "$old_jar" ]; then
        log "Removing old JAR file: $old_jar"
        rm -f "$old_jar"
    fi

    echo "$new_port" > "$PORT_FILE"
    log "Updated active port file to $new_port"

    log "Deployment completed successfully!"
    log "Active port: $new_port"
    log "Inactive port: $current_port"
}

main "$@"
