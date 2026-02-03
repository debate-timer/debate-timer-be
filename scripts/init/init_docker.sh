#!/bin/bash

set -e # 스크립트 실행 중 에러 발생 시 즉시 중단 (안전 장치)

DOCKER_USER=$1
DOCKER_TOKEN=$2

echo "=== Docker 설치를 시작합니다 ==="

echo "1. 필수 패키지 설치 및 GPG 키 설정 진행"
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg --yes
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "2. 리포지토리 설정 진행"
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

echo "3. Docker Engine 설치 진행"
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "4. 사용자 권한 설정 진행"
sudo usermod -aG docker $USER

echo "5. 로그인 진행 (변수 입력했을 경우에만)"
if [ -n "$DOCKER_USER" ] && [ -n "$DOCKER_TOKEN" ]; then
    echo "입력된 정보로 Docker Hub 로그인을 시도합니다..."
    echo "$DOCKER_TOKEN" | sg docker -c "docker login -u $DOCKER_USER --password-stdin"
    
    if [ $? -eq 0 ]; then
        echo "✅ 로그인 성공! (config.json이 생성되었습니다)"
    else
        echo "❌ 로그인 실패. 아이디/토큰을 확인하거나 'newgrp docker' 후 다시 시도하세요."
    fi
else
    echo "로그인 정보가 입력되지 않아 로그인을 건너뜁니다."
    echo "추후 'docker login' 명령어로 로그인하세요."
fi

echo "6. Docker 권한이 적용된 새로운 쉘로 전환"
exec newgrp docker