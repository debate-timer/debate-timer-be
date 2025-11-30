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

log() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "${timestamp} $@" | tee -a "$LOG_FILE"
}

error_exit() {
    log "$1"
    exit 1
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

health_check() {
    local port=$1
    local monitor_port=$2
    local health_url="http://localhost:$monitor_port/monitoring/health"

    log "Starting health check for port $port (monitor: $monitor_port)"

    local retry=1
    while [ $retry -le $MAX_HEALTH_CHECK_RETRIES ]; do
        local status=$(curl -s -o /dev/null -w "%{http_code}" "$health_url" 2>/dev/null || echo "000")

        log "Health check attempt $retry/$MAX_HEALTH_CHECK_RETRIES - Status: $status"

        if [ "$status" = "200" ]; then
            log "Health check passed!"
            return 0
        fi

        sleep $HEALTH_CHECK_INTERVAL
        retry=$((retry + 1))
    done

    log "Health check failed after $MAX_HEALTH_CHECK_RETRIES attempts"
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

    log "Starting application on port $port with JAR: $jar_file"

    if is_port_in_use "$port"; then
        log "Port $port is in use, cleaning up..."
        kill_process_on_port "$port"
    fi

    sudo nohup java \
        -Dspring.profiles.active=$PROFILE,monitor \
        -Duser.timezone=$TIMEZONE \
        -Dserver.port=$port \
        -Dmanagement.server.port=$monitor_port \
        -Ddd.service=debate-timer \
        -Ddd.env=$PROFILE \
        -jar "$jar_file" > "$APP_DIR/app-$port.log" 2>&1 &

    local pid=$!
    log "Application started with PID: $pid"

    sleep 3

    if ! kill -0 $pid 2>/dev/null; then
        error_exit "Application process died immediately after start. Check logs at $APP_DIR/app-$port.log"
    fi
}

switch_nginx_upstream() {
    local new_port=$1
    local nginx_conf="/etc/nginx/sites-available/api.dev.debate-timer.com"
    local temp_conf="/tmp/api.dev.debate-timer.com.tmp"

    if [ ! -f "$nginx_conf" ]; then
        error_exit "nginx configuration not found at $nginx_conf"
    fi

    log "Switching nginx upstream to port $new_port"

    sed "s/server 127\.0\.0\.1:[0-9]\+;/server 127.0.0.1:$new_port;/" "$nginx_conf" > "$temp_conf"

    sudo cp "$temp_conf" "$nginx_conf"
    if ! sudo nginx -t 2>/dev/null; then
        log "nginx configuration test failed"
        git checkout "$nginx_conf" 2>/dev/null || true
        return 1
    fi

    sudo nginx -s reload
    log "nginx reloaded successfully"

    sleep 2
    local response=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost/" 2>/dev/null || echo "000")
    if [ "$response" = "000" ] || [ "$response" = "502" ] || [ "$response" = "503" ]; then
        log "nginx health check failed after reload (status: $response)"
        return 1
    fi

    log "nginx is now routing traffic to port $new_port"
    return 0
}

main() {
    local current_port=$(get_current_port)
    local new_port=$(get_inactive_port "$current_port")
    local new_monitor_port=$(get_monitor_port "$new_port")

    log "Current active port: $current_port"
    log "Deploying to port: $new_port"
    log "Monitor port: $new_monitor_port"

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
    kill_process_on_port "$current_port"

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
