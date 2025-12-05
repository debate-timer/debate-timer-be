#!/bin/bash

set -e

NGINX_CONF="/etc/nginx/sites-available/api.dev.debate-timer.com"
BACKUP_CONF="/etc/nginx/sites-available/api.dev.debate-timer.com.backup"
TEMP_CONF="/tmp/api.dev.debate-timer.com.tmp"

log() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "${timestamp} $@"
}

if [ -z "$1" ]; then
    log "Usage: $0 <port>"
    log "Example: $0 8081"
    exit 1
fi

NEW_PORT=$1

if ! [[ "$NEW_PORT" =~ ^[0-9]+$ ]] || [ "$NEW_PORT" -lt 1 ] || [ "$NEW_PORT" -gt 65535 ]; then
    log "Invalid port number: $NEW_PORT"
    exit 1
fi

if [ ! -f "$NGINX_CONF" ]; then
    log "nginx configuration not found at $NGINX_CONF"
    exit 1
fi

log "Backing up current nginx configuration"
sudo cp "$NGINX_CONF" "$BACKUP_CONF"

log "Updating nginx upstream to port $NEW_PORT"
sed "s/server 127\.0\.0\.1:[0-9]\+;/server 127.0.0.1:$NEW_PORT;/" "$NGINX_CONF" > "$TEMP_CONF"

log "Configuration changes:"
diff "$NGINX_CONF" "$TEMP_CONF" || true

sudo cp "$TEMP_CONF" "$NGINX_CONF"

log "Testing nginx configuration"
if ! sudo nginx -t 2>&1; then
    log "nginx configuration test failed!"
    log "Rolling back to previous configuration"
    sudo cp "$BACKUP_CONF" "$NGINX_CONF"
    exit 1
fi

log "Reloading nginx"
sudo nginx -s reload

sleep 2
HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost/monitoring/health" 2>/dev/null || echo "000")

if [ "$HEALTH_STATUS" = "200" ]; then
    log "nginx successfully switched to port $NEW_PORT"
    log "Health check: OK (status $HEALTH_STATUS)"
    rm -f "$TEMP_CONF"
    exit 0
else
    log "Health check failed after nginx reload (status: $HEALTH_STATUS)"
    log "nginx may not be routing to the correct backend"
    exit 1
fi
