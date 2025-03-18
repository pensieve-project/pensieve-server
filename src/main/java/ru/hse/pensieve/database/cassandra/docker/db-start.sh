#!bin/bash

docker-compose up -d
sleep 10
docker exec -it cassandra bash -c "sed -i 's/materialized_views_enabled: false/materialized_views_enabled: true/' /etc/cassandra/cassandra.yaml"
docker exec -it cassandra bash -c "sed -i 's/materialized_views_enabled: false/materialized_views_enabled: true/' /etc/cassandra/cassandra_latest.yaml"
docker restart cassandra