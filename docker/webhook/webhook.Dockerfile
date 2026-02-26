FROM almir/webhook:2.8.3
USER root
RUN apk add --no-cache docker-cli docker-cli-compose curl bash util-linux
WORKDIR /etc/webhook
