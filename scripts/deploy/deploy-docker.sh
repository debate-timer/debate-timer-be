#!/bin/bash

TARGET_ENV=${ENV:-dev}
PROJECT_DIR="/home/ubuntu/docker"
TARGET_SERVICE="application"

cd $PROJECT_DIR || { echo "디렉토리 이동 실패"; exit 1; }

echo "--- 중단 배포 시작 ENV: $TARGET_ENV at $(date) ---"
export ENV=$TARGET_ENV

echo "최근 이미지 가져오는 중 (ENV: $TARGET_ENV)"
docker-compose pull $TARGET_SERVICE
if docker ps --format '{{.Names}}' | grep -q "^${TARGET_SERVICE}$"; then
    echo "기존 $TARGET_SERVICE 중지 및 제거 중..."
    docker-compose stop $TARGET_SERVICE
fi

echo "새 컨테이너 실행 중 - $TARGET_SERVICE..."
docker-compose up -d --no-deps $TARGET_SERVICE

echo "헬스 체크 진행 중 - $TARGET_SERVICE"
MAX_RETRIES=100
SLEEP_SECOND=10
COUNT=0

while [ $COUNT -lt $MAX_RETRIES ]; do
    HEALTH_STATUS=$(docker inspect --format='{{.State.Health.Status}}' "$TARGET_SERVICE" 2>/dev/null)
    if [ "$HEALTH_STATUS" = "healthy" ]; then
        echo "헬스 체크 완료 - $TARGET_SERVICE"
        break
    fi

    echo "헬스체크 진행 중 ($COUNT/$MAX_RETRIES) - 현재 상태: $HEALTH_STATUS"
    sleep $SLEEP_SECOND
    COUNT=$((COUNT + 1))
done

if [ $COUNT -eq $MAX_RETRIES ]; then
    echo "배포 실패: $TARGET_SERVICE가 healthy 상태가 되지 않았습니다."
    docker logs --tail 50 $TARGET_SERVICE
    exit 1
fi

docker image prune -f

echo "--- $TARGET_ENV 배포 완료 at $(date) ---"
