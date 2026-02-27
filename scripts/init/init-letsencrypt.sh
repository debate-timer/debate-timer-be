#!/bin/bash

cd "$(dirname "$0")" || exit 1

mkdir -p ../../letsencrypt
touch ../../letsencrypt/acme.json
chmod 600 ../../letsencrypt/acme.json

echo "letsencrypt 폴더를 생성했습니다."
