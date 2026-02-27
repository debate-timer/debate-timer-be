FROM almir/webhook:2.8.3
USER root
RUN apk add --no-cache docker-cli-compose curl bash util-linux git

WORKDIR /etc/webhook

RUN git config --global --add safe.directory /home/ubuntu/debate-timer
