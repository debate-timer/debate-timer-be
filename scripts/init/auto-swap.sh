#!/bin/bash

set -e # 스크립트 실행 중 에러 발생 시 즉시 중단 (안전 장치)

# --- 설정 변수 ---
SWAP_FILE="/swapfile"
SWAP_SIZE=${1:-"2G"}
FSTAB_FILE="/etc/fstab"

# 1. 루트 권한 확인
if [ "$EUID" -ne 0 ]; then
  echo "오류: 이 스크립트는 root 권한(sudo)으로 실행해야 합니다."
  exit 1
fi

# 2. 기존 스왑 파일 존재 여부 확인
if [ -f "$SWAP_FILE" ]; then
  echo "알림: $SWAP_FILE 이 이미 존재합니다."
  echo "작업을 중단합니다."
  exit 0
fi

echo "=== Swap 메모리 생성 시작 ($SWAP_SIZE) ==="

# 3. Swap 파일 생성
fallocate -l $SWAP_SIZE $SWAP_FILE
echo " -> 파일 생성 완료"

# 4. 권한 설정 (600)
chmod 600 $SWAP_FILE
echo " -> 권한 설정 완료"

# 5. Swap 활성화
mkswap $SWAP_FILE > /dev/null
swapon $SWAP_FILE
echo " -> Swap 활성화 완료"

# 6. /etc/fstab 등록 (재부팅 후에도 유지)
if ! grep -q "$SWAP_FILE" "$FSTAB_FILE"; then
    echo "$SWAP_FILE swap swap defaults 0 0" >> "$FSTAB_FILE"
    echo " -> fstab 등록 완료 (자동 실행 설정)"
fi

echo "=== 모든 작업이 완료되었습니다 ==="
echo "[현재 메모리 상태]"
free -h
