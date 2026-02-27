#!/bin/bash

if [ -z "$SECRET_TOKEN" ] || [ "$REQUEST_TOKEN" != "$SECRET_TOKEN" ]; then
    echo "[$(date)] 🚨 보안 경고: 유효하지 않은 배포 요청입니다. (Unauthorized)"
    exit 1
fi

echo "스크립트 실행 여부를 확인합니다."
LOCK_FILE="/tmp/deploy-application.lock"
exec 200>"$LOCK_FILE"
flock -n 200 || { echo "⚠️ 이미 배포 스크립트가 실행 중입니다. 중복 실행을 차단합니다."; exit 1; }
echo "스크립트를 실행하고 있지 않습니다. 배포를 시작합니다."

TARGET_ENV=${ENV:-dev}
PROJECT_DIR="/home/ubuntu/debate-timer"
DOCKER_DIR="/home/ubuntu/debate-timer/docker"
TARGET_SERVICE="application"

echo "--- Git 저장소 최신화 시작 ---"
cd $PROJECT_DIR || { echo "프로젝트 디렉토리 이동 실패"; exit 1; }

if [ "$TARGET_ENV" = "prod" ]; then
    echo "▶ [ENV: prod] main 브랜치로 이동하여 최신 코드를 가져옵니다."
    git fetch origin main
    git switch main
    git reset --hard origin/main # 로컬 변경사항 무시하고 원격과 완벽히 일치시킴
elif [ "$TARGET_ENV" = "dev" ]; then
    echo "▶ [ENV: dev] develop 브랜치로 이동하여 최신 코드를 가져옵니다."
    git fetch origin develop
    git switch develop
    git reset --hard origin/develop
else
    CURRENT_BRANCH=$(git branch --show-current)
    if [ -z "$CURRENT_BRANCH" ]; then
        echo "❌ 현재 브랜치를 확인할 수 없습니다 (detached HEAD 상태일 수 있음)"
        exit 1
    fi
    echo "▶ [ENV: $TARGET_ENV] 현재 브랜치($CURRENT_BRANCH)에서 최신 코드를 가져옵니다."
    git fetch origin "$CURRENT_BRANCH"
    git reset --hard origin/"$CURRENT_BRANCH"
fi

cd $DOCKER_DIR || { echo "디렉토리 이동 실패"; exit 1; }
echo "--- 중단 배포 시작 ENV: $TARGET_ENV at $(date) ---"
export ENV=$TARGET_ENV

echo "최근 이미지 가져오는 중 (ENV: $TARGET_ENV)"
docker compose pull $TARGET_SERVICE || { echo "❌ 이미지 풀 실패"; exit 1; }
if docker ps --format '{{.Names}}' | grep -q "^${TARGET_SERVICE}$"; then
    echo "기존 $TARGET_SERVICE 중지 및 제거 중..."
    docker compose stop $TARGET_SERVICE
fi

echo "새 컨테이너 실행 중 - $TARGET_SERVICE..."
docker compose up -d --no-deps $TARGET_SERVICE

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
