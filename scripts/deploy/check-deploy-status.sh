#!/bin/bash

LOCK_FILE="/tmp/deploy-application.lock"

exec 200>"$LOCK_FILE"

if ! flock -n 200; then
    echo '{"status": "running", "message": "배포 스크립트가 현재 실행 중입니다."}'
else
    flock -u 200
    echo '{"status": "idle", "message": "현재 진행 중인 배포가 없습니다. 대기 중입니다."}'
fi

exec 200>&-
