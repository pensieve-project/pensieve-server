#!bin/bash

docker-compose build
docker-compose up -d
sleep 20
docker cp elasticsearch:/usr/share/elasticsearch/config/certs/http_ca.crt ../../logstash/docker/http_ca.crt
