#!/bin/bash

cd "$(dirname "$0")" || exit 1

mkdir -p ../../letsencrypt
touch ../../letsencrypt/acme.json
chmod 600 ../../letsencrypt/acme.json

echo "✅ 스크립트 위치($(pwd))에 letsencrypt 폴더를 생성했습니다."
