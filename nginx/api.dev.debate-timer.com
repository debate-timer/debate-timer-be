# 서버 /etc/nginx/sites-available/api.dev.debate-timer.com 과 같은 내용이다. (2026-11-08 기준)
# proxy_pass 포트(8080/8081)는 무중단 배포 스크립트가 배포할 때마다 바꾼다.
server {
    server_name api.dev.debate-timer.com; #허용할 서버 도메인


    location / {
        proxy_pass http://127.0.0.1:8081; #301로 리다이렉트하면 POST도 GET으로 전달됨.
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        # 클라이언트가 보낸 값을 그대로 넘기지 않도록 nginx 가 아는 값으로 덮어쓴다
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
    }

    location /ws/ {
        proxy_pass http://127.0.0.1:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        # 클라이언트가 보낸 값을 그대로 넘기지 않도록 nginx 가 아는 값으로 덮어쓴다
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        proxy_read_timeout 3600s;
    }

    listen [::]:443 ssl ipv6only=on; # managed by Certbot
    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/api.dev.debate-timer.com/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/api.dev.debate-timer.com/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

}
server {
    if ($host = api.dev.debate-timer.com) {
        return 308 https://$host$request_uri;
    } # managed by Certbot


    listen 80;
    listen [::]:80;
    server_name api.dev.debate-timer.com;
    return 404; # managed by Certbot


}
