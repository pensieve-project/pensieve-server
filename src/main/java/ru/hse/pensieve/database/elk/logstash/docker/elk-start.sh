#!/bin/bash

docker-compose build
docker-compose up -d
docker exec -it logstash bash -c "sed -i 's/http:/https:/' /usr/share/logstash/config/logstash.yml"