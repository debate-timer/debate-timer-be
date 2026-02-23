#!/bin/bash

TARGET_ENV=${ENV:-dev}
PROJECT_DIR="/home/ubuntu/docker"

cd $PROJECT_DIR || { echo "디렉토리 이동 실패"; exit 1; }

echo "--- 배포 시작 ENV: $TARGET_ENV at $(date) ---"

# 현재 실행 중인 색상 확인
if docker ps --filter "status=running" --format '{{.Names}}' | grep -q "app-blue"; then
    CURRENT_SERVICE="app-blue"
    TARGET_SERVICE="app-green"
elif docker ps --filter "status=running" --format '{{.Names}}' | grep -q "app-green"; then
    CURRENT_SERVICE="app-green"
    TARGET_SERVICE="app-blue"
else
    CURRENT_SERVICE=""
    TARGET_SERVICE="app-blue"
fi

echo "현재 실행중인 서비스: ${CURRENT_SERVICE:-없음}"
echo "앞으로 띄울 서비스: $TARGET_SERVICE"
export ENV=$TARGET_ENV

echo "최근 이미지 가져오는 중 (ENV: $TARGET_ENV)"
docker-compose pull $TARGET_SERVICE

echo "컨테이너 실행 중 - $TARGET_SERVICE..."
docker-compose up -d --no-deps $TARGET_SERVICE

echo "헬스 체크 진행 중 - $TARGET_SERVICE"
MAX_RETRIES=90
SLEEP_SECOND=10
COUNT=0

while [ $COUNT -lt $MAX_RETRIES ]; do
    # 컨테이너의 Health 상태 추출
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
    echo "배포 실패: $TARGET_SERVICE did not become healthy."
    docker-compose stop $TARGET_SERVICE
    exit 1
fi

if [ ! -z "$CURRENT_SERVICE" ]; then
    echo "이전 서비스 $CURRENT_SERVICE 중지 중..."
    docker-compose stop $CURRENT_SERVICE
else
    echo "이전 서비스가 없으므로 스위칭 단계 건너뜁니다."
fi

docker image prune -f

echo "--- $TARGET_ENV 배포 완료 at $(date) ---"
